package academy.kafka;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.PaymentRequest;
import academy.kafka.entities.Registration;
import academy.kafka.serdes.AppSerdes;

/**
 * We read registrations and generate payment requests
 * This solution is not idempotent
 */
public class PaymentRequest01Tryout1 {
        static int rn = ThreadLocalRandom.current().nextInt(1000);
       
        static List<KeyValue<String, PaymentRequest>> generatePaymentRequests(Registration registration) {
                List<KeyValue<String, PaymentRequest>> result = new ArrayList<>();
                for (LocalDate date = registration.getFrom(); date
                                .isBefore(LocalDate.now()); date = date.plusMonths(3)) {
                        System.out.println(date);
                        PaymentRequest pr = PaymentRequest.generatePaymentRequest(registration, date,
                                        date.plusMonths(3));
                        result.add(KeyValue.pair(pr.getKey(), pr));
                }

                return result;
        }

        public static void main(String[] args) {
                Properties props = new Properties();
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "payment_request" + rn);
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

                StreamsBuilder builder = new StreamsBuilder();
                KStream<String, Registration> registrations = builder
                                .stream(Registration.topicName,
                                                Consumed.with(AppSerdes.String(), AppSerdes.Registration()));
                 
                registrations.peek((id, registration) -> {
                        System.out.println(id + " " + registration.getFrom());
                });
                KStream<String, PaymentRequest> paymentRequests = registrations
                                .flatMap((key, registration) -> generatePaymentRequests(registration));
                paymentRequests.print(Printed.toSysOut());
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
