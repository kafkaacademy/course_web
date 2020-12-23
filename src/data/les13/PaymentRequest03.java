package academy.kafka;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.Day;
import academy.kafka.entities.PaymentRequest;
import academy.kafka.entities.Registration;
import academy.kafka.serdes.AppSerdes;

/*
different solution... now joining with days
*/
public class PaymentRequest03 {
        static int rn = ThreadLocalRandom.current().nextInt(1000);

        static PaymentRequest createPaymentRequest(Registration registration, Day day) {
                return PaymentRequest.generatePaymentRequest(registration, day.getDay(), day.getDay().plusMonths(3));
        }

        public static void main(String[] args) {
                Properties props = new Properties();
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "payment_request" + rn);// !!!! NOT RANDOM ??
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

                StreamsBuilder builder = new StreamsBuilder();
                KStream<String, Registration> registrations = builder.stream(Registration.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Registration()));
                registrations = registrations.selectKey((k, v) -> v.getNewPaymentRequestDate().toString());
                registrations.peek((k, v) -> System.out.println(" key=" + v.getKey()));

                KTable<String, Day> dayTbl = builder.table(Day.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Day()));
                KStream<String, PaymentRequest> paymentRequests = registrations.join(dayTbl,
                                (reg, day) -> createPaymentRequest(reg, day));
                paymentRequests = paymentRequests.selectKey((k, v) -> v.getKey());
                paymentRequests.peek((k, v) -> System.out.println("key=" + k + " " + v));
                paymentRequests.to(PaymentRequest.topicName,
                                Produced.with(AppSerdes.String(), AppSerdes.PaymentRequest()));

                KafkaStreams streams = new KafkaStreams(builder.build(), props);
                streams.start();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Stopping Streams...");
                        streams.close();
                }));
        }
}
