package academy.kafka;

import java.util.Properties;
import java.util.Random;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.PaymentIntake;
import academy.kafka.serdes.AppSerdes;

public class IdemPotent01ShowPaymentIntake {
        static final Random rn = new Random();// helper, remove in production

        public static void main(String[] args) {
                Properties props = new Properties();
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "idempotency_test" + rn.nextInt(10000));
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

                StreamsBuilder builder = new StreamsBuilder();
                KStream<String, PaymentIntake> payments = builder.stream(PaymentIntake.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.PaymentIntake()));
                payments.peek((id, payment) -> {
                        System.out.println(id + " " + payment.getAmount());
                });

                KafkaStreams streams = new KafkaStreams(builder.build(), props);
                streams.start();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Stopping Streams...");
                        streams.close();
                }));
        }
}
