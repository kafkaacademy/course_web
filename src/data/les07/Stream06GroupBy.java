package academy.kafka;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.Person;
import academy.kafka.serdes.AppSerdes;

/**
 * In this exercise we focus on groupbykey (note , grouping can take some time to initialize)
 * 
 */
public class Stream06GroupBy {
    static Random rn = new Random();// helper, remove in production

    public static void main(final String[] args) {
        System.out.println("Be patient, groupby needs time to initialize");
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamFilter" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, Person> persons = builder.stream(Person.topicName,
                Consumed.with(AppSerdes.String(), AppSerdes.Person()));
        KGroupedStream<String, Person> groupedOnBsn = persons.groupByKey();
        KTable<String, Long> tblOnBsn = groupedOnBsn
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-on-bsn"));
        tblOnBsn.toStream().peek((k, v) -> System.out.printf("bsn:%s \tnumber:%s\n", k, v));

        KStream<String, Person> personsOnProvince = persons.selectKey((bsn, person) -> person.getProvince().getName());
        personsOnProvince.peek((k, v) -> System.out.printf("province:%s \tperson:%s\n", k, v));
        KGroupedStream<String, Person> groupedOnProvince = personsOnProvince.groupByKey();
        // uncomment the following line and.... we crash
        // KTable<String, Long> tblOnProvince = groupedOnProvince.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-on-province"));
        // we are doing same count here as above (also on <String,Person>)
        // but we violate here some rule of Apache Kafka !! Which one??    
       
        final Topology topology = builder.build();

        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
