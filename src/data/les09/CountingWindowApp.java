package academy.kafka;

import java.time.Duration;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.PaymentRequest;
import academy.kafka.serdes.AppSerdes;
import academy.kafka.utils.KafkaUtils;

/*TODO counts are not ok */

public class CountingWindowApp {
    private static final Logger logger = LogManager.getLogger();
    static Random rn = new Random();// helper, remove in production

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "counting" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, PaymentRequest> KS0 = streamsBuilder.stream(PaymentRequest.topicName,
                Consumed.with(AppSerdes.String(), AppSerdes.PaymentRequest())
                        .withTimestampExtractor(new PaymentRequestTimeExtractor()));

        KTable<Windowed<String>, Long> KT0 = KS0
                .groupByKey(Grouped.with(AppSerdes.String(), AppSerdes.PaymentRequest()))
                .windowedBy(TimeWindows.of(Duration.ofDays(28))).count();

        KT0.toStream().foreach(
                (key, value) -> System.out.println(" Window start: " + KafkaUtils.formatTimestamp(key.window().start())
                        +
                    " Window end: " + KafkaUtils.formatTimestamp(key.window().end()) +
                    " Count: " + value
            )
        );


        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping Streams");
            streams.close();
        }));

    }
}
