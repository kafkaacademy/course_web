package academy.kafka;

import java.time.LocalDate;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.Day;
import academy.kafka.entities.PaymentRequest;
import academy.kafka.entities.Registration;
import academy.kafka.serdes.AppSerdes;

/*
Now robust for partioning (???)
why it is not working ....

The join will be triggered under the conditions listed below whenever new input is received. When it is triggered, the user-supplied ValueJoiner will be called to produce join output records.

Only input records for the left side (stream) trigger the join. Input records for the right side (table) update only the internal right-side join state.
Input records for the stream with a null key or a null value are ignored and do not trigger the join.
Input records for the table with a null value are interpreted as tombstones, which indicate the deletion of a record key from the table. Tombstones do not trigger the join.

So use solution 5 till this restriction is removed!!

And topic registrations and topic days should have 1 partition!!!

*/
public class PaymentRequest06GlobalTable {

        static int startYear = 2010;// before any registration
        static int pauseForNextDay = 20;// msec

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
                LocalDate newDate = day.getDay().plusMonths(3);
                boolean newReg = true;
                LocalDate till = registration.getTill();
                if (till == null)
                        registration.setNewPaymentRequestDate(newDate);
                else {
                        if (till.isAfter(newDate))
                                registration.setNewPaymentRequestDate(newDate);
                        else
                                newReg = false;
                }
                LocalDate endDate = newDate;
                if (till != null && till.isBefore(endDate))
                        endDate = till;
                PaymentRequest pr = PaymentRequest.generatePaymentRequest(registration, day.getDay(), endDate);
                return new TemporaryContainer((newReg) ? registration : null, pr);
        }

        public static void main(String[] args) {
                academy.kafka.utils.KafkaUtils.deleteTopic(Day.topicName);
                academy.kafka.utils.KafkaUtils.createTopic(Day.topicName, 1, 1);
                new UpdateDay(pauseForNextDay, startYear).start();
                
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

                GlobalKTable<String, Day> dayTbl = builder.globalTable(Day.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Day()));
                          
                KStream<String, TemporaryContainer> tmps = registrations.join(dayTbl,
                                (regKey, registration) -> registration.getNewPaymentRequestDate().toString(),
                                (registration, day) -> createTmp(registration, day));
                KStream<String, PaymentRequest> resultPaymentRequests = tmps
                                .map((k, v) -> KeyValue.pair(v.paymentRequest.getKey(), v.paymentRequest));
                KStream<String, Registration> resultRegistrations = tmps.filter((k, v) -> v.registration != null)
                                .map((k, v) -> KeyValue.pair(v.registration.getKey(), v.registration));
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
