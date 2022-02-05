package academy.kafka;

import java.util.Properties;
import java.util.Random;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.PaymentIntake;
import academy.kafka.serdes.AppSerdes;

public class IdemPotent04Branching {
        static final Random rn = new Random();// helper, remove in production

        public static void main(String[] args) {
                Properties props = new Properties();
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "idempotency_test" + rn.nextInt(10000));
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

                StreamsBuilder builder = new StreamsBuilder();

                KStream<String, PaymentIntake> payments = builder.stream(PaymentIntake.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.PaymentIntake()));
                KGroupedStream<String, PaymentIntake> paymentsGrouped = payments.groupByKey();
                KTable<String, Long> paymentsTbl = paymentsGrouped.count();
                KStream<String, Long> paymentsInspection = paymentsTbl.toStream();

                KStream<String, Long>[] branch = paymentsInspection.branch(
                        (id, count) -> count == 1,
                        (id, count) -> count > 1);

                KStream<String, Long> ok = branch[0];
                KStream<String, Long> notOk = branch[1];
                ok.peek((id, count) -> {
                        System.out.println(id + " ok ");
                });
                notOk.peek((id, count) -> {
                        System.out.println(id + " not ok ");
                });
                KafkaStreams streams = new KafkaStreams(builder.build(), props);
                streams.start();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Stopping Streams...");
                        streams.close();
                }));
        }
}
