package academy.kafka;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.PaymentRequest;
import academy.kafka.entities.Registration;
import academy.kafka.serdes.AppSerdes;

/**
 * Is this solution idempotent?? 
 * This is with a kind of "double bookkeeping".
 * Still a problem , as time passes by , new period for payment is coming, How to solve that?

 */
public class PaymentRequest02Tryout2 {
        static final Random rn = new Random();// helper, remove in production

        static LocalDate till = LocalDate.now();

        static List<KeyValue<String, PaymentRequest>> generatePaymentRequests(Registration registration) {
                List<KeyValue<String, PaymentRequest>> result = new ArrayList<>();
                for (LocalDate date = registration.getNewPaymentRequestDate(); date
                                .isBefore(till); date = date.plusMonths(3)) {
                        PaymentRequest pr = PaymentRequest.generatePaymentRequest(registration, date,
                                        date.plusMonths(3));
                        registration.setNewPaymentRequestDate(date.plusMonths(3));

                        result.add(KeyValue.pair(pr.getKey(), pr));
                }

                return result;
        }

        public static void main(String[] args) {
                Properties props = new Properties();
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "payment_request");// !!!! NO RANDOM
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

                StreamsBuilder builder = new StreamsBuilder();
                KStream<String, Registration> registrations = builder
                                .stream(Registration.topicName,
                                                Consumed.with(AppSerdes.String(), AppSerdes.Registration()))
                                .filter((k, v) -> v.getNewPaymentRequestDate().isBefore(till));

                KStream<String, PaymentRequest> paymentRequests = registrations
                                .flatMap((key, registration) -> generatePaymentRequests(registration));
                paymentRequests.to(PaymentRequest.topicName,
                                Produced.with(AppSerdes.String(), AppSerdes.PaymentRequest()));
                registrations.peek((id, registration) -> {
                        System.out.println(registration.getKey() + " " + id + " "
                                        + registration.getNewPaymentRequestDate());
                });
                registrations.to(Registration.topicName, Produced.with(AppSerdes.String(), AppSerdes.Registration()));

                KafkaStreams streams = new KafkaStreams(builder.build(), props);
                streams.start();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Stopping Streams...");
                        streams.close();
                }));
        }
}
