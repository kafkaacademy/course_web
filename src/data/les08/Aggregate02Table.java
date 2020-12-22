package academy.kafka;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.Person;
import academy.kafka.entities.ProvinceAggregate;
import academy.kafka.serdes.AppSerdes;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.state.KeyValueStore;


public class Aggregate02Table {
    static Random rn = new Random();// helper, remove in production

    public static void main(final String[] args) {
        System.out.println("...please wait till I am ready with building up the table");

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "tableAggregateApp" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        StreamsBuilder builder = new StreamsBuilder();

        builder.table(Person.topicName, Consumed.with(AppSerdes.String(), AppSerdes.Person()))
                .groupBy((k, v) -> KeyValue.pair(v.getProvince().getName(), v),
                        Grouped.with(AppSerdes.String(), AppSerdes.Person()))
                .aggregate(
                        // Initializer
                        () -> new ProvinceAggregate(null, 0),
                        // Adder
                        (k, v, aggValue) -> {
                            System.out.println("key: " + k + ", Prov: " + v.getProvince().getName() + ": add " + v.getLastName());
                            return new ProvinceAggregate(v.getProvince().getName(), aggValue.getTotal()+1);
                        },
                        // Subtractor
                        (k, v, aggValue) -> {
                            System.out.println("key: " + k + ", Prov: " + v.getProvince().getName() + ": subtract " + v.getLastName());
                            return new ProvinceAggregate(v.getProvince().getName(), aggValue.getTotal()-1);
                        },
                        // Serializer
                        Materialized.<String, ProvinceAggregate, KeyValueStore<Bytes, byte[]>>as("stateStoreName")
                                .withValueSerde(AppSerdes.ProvinceAggregate()))
                .toStream().print(Printed.<String, ProvinceAggregate>toSysOut().withLabel("Province Aggregate"));

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
