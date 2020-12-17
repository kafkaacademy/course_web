package academy.kafka;

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

import academy.kafka.config.AppConfig;
import academy.kafka.entities.PaymentRequest;
import academy.kafka.entities.ProvinceAggregate;
import academy.kafka.serdes.AppSerdes;

/**
 * Table aggregates work on streams like change logs, only the last record keeps it's value
 * Note the substractor : records/messages with null value are deleted
 */
public class Aggregate02Table {
    static Random rn = new Random();// helper, remove in production

    public static void main(final String[] args) {
        System.out.println("...please wait till I am ready with building up the table");

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "tableAggregateApp" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        StreamsBuilder builder = new StreamsBuilder();

        builder.table(PaymentRequest.topicName, Consumed.with(AppSerdes.String(), AppSerdes.PaymentRequest()))
                .groupBy((k, v) -> KeyValue.pair(v.getProvinceName(), v),
                        Grouped.with(AppSerdes.String(), AppSerdes.PaymentRequest()))
                .aggregate(
                        // Initializer
                        () -> new ProvinceAggregate(null, 0),
                        // Adder
                        (k, v, aggValue) -> new ProvinceAggregate(v.getProvinceName(),
                                aggValue.getTotal() + v.getAmount()),
                        // Subtractor
                        (k, v, aggValue) -> new ProvinceAggregate(v.getProvinceName(),
                                aggValue.getTotal() - v.getAmount()),
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
