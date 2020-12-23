package academy.kafka;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
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

/**Note the recursion on registrations: therefore we need to be carefull */
public class PaymentRequest04 {

        public static class TemporaryContainer {
                Registration registration;
                PaymentRequest paymentRequest;

                public TemporaryContainer(Registration registration, PaymentRequest paymentRequest) {
                        this.registration = registration;
                        this.paymentRequest = paymentRequest;
                }
        }

        static int rn = ThreadLocalRandom.current().nextInt(1000);

        static TemporaryContainer createTmp(Registration registration, Day day) {
                registration.setNewPaymentRequestDate(day.getDay().plusMonths(3));
                PaymentRequest pr = PaymentRequest.generatePaymentRequest(registration, day.getDay(),
                                day.getDay().plusMonths(3));
                return new TemporaryContainer(registration, pr);
        }

        public static void main(String[] args) {
                academy.kafka.utils.KafkaUtils.deleteTopic(Day.topicName);
                academy.kafka.utils.KafkaUtils.createTopic(Day.topicName, 1, 1);
                UpdateDay thread = new UpdateDay(100, 2017);//good starting point
                thread.start();
                Properties props = new Properties();
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "paymment_request" + rn);// !!!! Should be NO RANDOM
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

                StreamsBuilder builder = new StreamsBuilder();
                KStream<String, Registration> registrations = builder.stream(Registration.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Registration()));
                registrations.peek((k, v) -> System.out
                                .println("at start : key=" + k + " " + v.getNewPaymentRequestDate()));

                registrations = registrations.selectKey((k, v) -> v.getNewPaymentRequestDate().toString());

                KTable<String, Day> dayTbl = builder.table(Day.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Day()));
              //  dayTbl.toStream().peek((k, v) -> System.out
              //                  .println("day at start : day=" + k ));

                KStream<String, TemporaryContainer> tmps = registrations.join(dayTbl,
                                (reg, day) -> createTmp(reg, day));
                KStream<String, PaymentRequest> resultPaymentRequests = tmps
                                .map((k, v) -> KeyValue.pair(v.paymentRequest.getKey(), v.paymentRequest));
                KStream<String, Registration> resultRegistrations = tmps.filter((k, v) -> v.registration != null)
                                .map((k, v) -> KeyValue.pair(v.registration.getKey(), v.registration));

                resultRegistrations.peek((k, v) -> System.out.println("result : key=" + k + " " + v.getNewPaymentRequestDate()));
                resultRegistrations.to(Registration.topicName,
                                Produced.with(AppSerdes.String(), AppSerdes.Registration()));
                resultPaymentRequests.to(PaymentRequest.topicName,
                                Produced.with(AppSerdes.String(), AppSerdes.PaymentRequest()));

                KafkaStreams streams = new KafkaStreams(builder.build(), props);
                streams.start();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Stopping Streams...");
                        streams.close();
                }));
        }
}
