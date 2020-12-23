package academy.kafka;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.Car;
import academy.kafka.entities.Registration;
import academy.kafka.serdes.AppSerdes;

/*
Audit registration : is fk to car ok
(Child has Parent?)
*/
public class Audit01 {

        static int rn = ThreadLocalRandom.current().nextInt(1000);

        public static void main(String[] args) {
                Properties props = new Properties();
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "registration_car_audit" + rn);//here random allowed
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

                StreamsBuilder builder = new StreamsBuilder();
                GlobalKTable<String, Car> carTbl = builder.globalTable(Car.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Car()));
                KStream<String, Registration> registrations = builder.stream(Registration.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Registration()));

                KStream<String, String> result = registrations.leftJoin(carTbl,
                                (regKey, registration) -> registration.getCar().getKey(), (registration, car) -> {
                                        if (car != null)
                                                return "registration " + registration.getKey()
                                                                + " points to existing car "+ car.getKey();
                                        else
                                                return "registration " + registration.getKey() + " has no car";
                                });

                result.peek((k, v) -> {
                        System.out.println(v);
                });
                KafkaStreams streams = new KafkaStreams(builder.build(), props);
                streams.start();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Stopping Streams...");
                        streams.close();
                }));
        }
}
