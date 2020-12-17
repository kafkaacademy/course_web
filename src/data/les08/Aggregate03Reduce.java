package academy.kafka;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.Person;
import academy.kafka.serdes.AppSerdes;

/**
 * a little stupid example , the eldest person with a bsn.
 * More realistic : the eldest person of a province, but then we have to change the key and ... the crash
 */
public class Aggregate03Reduce {
    static Random rn = new Random();// helper, remove in production

    public static void main(final String[] args) {
        System.out.println("...please wait till I am ready with building up the table");

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamFilter" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Person> persons = builder.stream(Person.topicName,
                Consumed.with(AppSerdes.String(), AppSerdes.Person()));
       
        KGroupedStream<String, Person> grped = persons.groupByKey();
        KTable<String, Person> tbl = grped.reduce((agrPerson, newPerson) -> {if (newPerson.getBirthday().isBefore(agrPerson.getBirthday())) return newPerson;else return agrPerson;});
        tbl.toStream().print(Printed.toSysOut());

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
