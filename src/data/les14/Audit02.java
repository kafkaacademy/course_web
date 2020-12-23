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
import academy.kafka.entities.Person;
import academy.kafka.entities.Registration;
import academy.kafka.serdes.AppSerdes;

/*
Audit registration : is fk to person ok
(Child has Parent?)
*/
public class Audit02 {

        static int rn = ThreadLocalRandom.current().nextInt(1000);

        public static void main(String[] args) {
                Properties props = new Properties();
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "registation_person_audit" + rn);//here random allowed
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 0);

                StreamsBuilder builder = new StreamsBuilder();
                GlobalKTable<String, Person> parentTbl = builder.globalTable(Person.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Person()));
                KStream<String, Registration> registrations = builder.stream(Registration.topicName,
                                Consumed.with(AppSerdes.String(), AppSerdes.Registration()));

                KStream<String, String> result = registrations.leftJoin(parentTbl,
                                (regKey, registration) -> registration.getPerson().getKey(), (registration, parentObject) -> {
                                        if (parentObject != null)
                                                return "registration " + registration.getKey()
                                                                + " points to existing person "+parentObject.getKey();
                                        else
                                                return "registration " + registration.getKey() + " has no person";
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
