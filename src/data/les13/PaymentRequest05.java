package academy.kafka;

import java.time.LocalDate;
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

/*
the recursion that is needed : registration goes changed to Kafka
join Table-Table does reacts ok on changing underlying streams
join is re-evaluated
WORKING finally!! (but what when nr of partitions>1)

startyear should be before any registrations

with pause 10 to show progress


*/
public class PaymentRequest05 {

        static int startYear = 2012;// before any registration
        static int pauseForNextDay = 5;// msec

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
                UpdateDay thread = new UpdateDay(pauseForNextDay, startYear);
                thread.start();
                Properties props = new Properties();
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "paymment_request" + rn);// !!!! Should be NO RANDOM
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

                StreamsBuilder builder = new StreamsBuilder();
                KStream<String, Registration> registrations = builder.stream(Registration.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Registration()));
              //  registrations.peek((k, v) -> System.out
             //                   .println("at start : key=" + k + " " + v.getNewPaymentRequestDate()));

                KTable<String, Registration> regTbl = registrations
                                .selectKey((k, v) -> v.getNewPaymentRequestDate().toString()).toTable();

                KTable<String, Day> dayTbl = builder.table(Day.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Day()));
                KTable<String, TemporaryContainer> tmps = regTbl.join(dayTbl, (reg, day) -> createTmp(reg, day));
                KStream<String, PaymentRequest> resultPaymentRequests = tmps.toStream()
                                .map((k, v) -> KeyValue.pair(v.paymentRequest.getKey(), v.paymentRequest));
                KStream<String, Registration> resultRegistrations = tmps.toStream()
                                .filter((k, v) -> v.registration != null)
                                .map((k, v) -> KeyValue.pair(v.registration.getKey(), v.registration));

                 resultRegistrations.to(Registration.topicName,
                                Produced.with(AppSerdes.String(), AppSerdes.Registration()));
                resultPaymentRequests.to(PaymentRequest.topicName,
                                Produced.with(AppSerdes.String(), AppSerdes.PaymentRequest()));
                 resultPaymentRequests.peek((k, v) -> System.out
                               .println("created payment request : key=" + k + " " + v.getPeriodStart()+"-"+v.getPeriodEnd()));
          
                KafkaStreams streams = new KafkaStreams(builder.build(), props);
                streams.start();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Stopping Streams...");
                        streams.close();
                }));
        }
}
