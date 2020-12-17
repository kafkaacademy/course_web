package academy.kafka;

import java.time.Duration;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.PaymentRequest;
import academy.kafka.serdes.AppSerdes;
import academy.kafka.utils.GenericTimeExtractor;
import academy.kafka.utils.KafkaUtils;

public class AggregateWindowed {
    static Random rn = new Random();// helper, remove in production

    public static void main(final String[] args) {
        System.out.println("some patience, startup takes some time");
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamFilter" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, PaymentRequest> paymentRequests = builder
                .stream(PaymentRequest.topicName,
                        Consumed.with(AppSerdes.String(), AppSerdes.PaymentRequest())
                                .withTimestampExtractor(new GenericTimeExtractor<PaymentRequest>(
                                        paymentRequest -> KafkaUtils.localDateToLong(paymentRequest.getPeriodStart()))))
                .selectKey((key, paymentRequest) -> paymentRequest.getProvinceName());

        KTable<Windowed<String>, Long> groupby28Days = paymentRequests
                .groupByKey(Grouped.with(AppSerdes.String(), AppSerdes.PaymentRequest()))
                .windowedBy(TimeWindows.of(Duration.ofDays(28))).count();// we choose 30 days because we cannot choose here a generic month
               
                groupby28Days.toStream()
                .foreach((wKey,
                        value) -> System.out.println(String.format("%13s Window:%11s start %s end %s count %1d",
                                wKey.key(), wKey.window().hashCode(), KafkaUtils.formatTimestamp(wKey.window().start()),
                                KafkaUtils.formatTimestamp(wKey.window().end()), value)));
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
