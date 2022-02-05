package academy.kafka;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.Notification;
import academy.kafka.entities.Person;
import academy.kafka.entities.Province;
import academy.kafka.serdes.AppSerdes;

public class Stream04MapValues {
    static Random rn = new Random();// helper, remove in production

    /**
     * Move all persons living in Amsterdam to Utrecht sent a notification of the change and store the change
     * Repeat this.
     * Notice the recursion!!! Be always aware of this!!
    */
    static String oldProvince="Limburg";
    static String newProvince="Utrecht";
    public static void main(final String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamFilter" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, Person> persons = builder.stream(Person.topicName,
                Consumed.with(AppSerdes.String(), AppSerdes.Person()));
                persons.peek((bsn, person) -> System.out.println("bsn " + bsn + " province= " + person.getProvince().getName()));
      
        KStream<String, Person> personsMoved = persons
                .filter((bsn, person) -> person.getProvince().getName().equals(oldProvince))
                .mapValues((v) -> {
                    v.setProvince(Province.getProvinceOnName(newProvince));
                    return v;
                });
                personsMoved.peek((bsn, person) -> System.out.println("key" + bsn + " value " + person.getLastName()));
      
        KStream<String, Notification> notifications = personsMoved
                .mapValues((person) -> new Notification(person.getBsn(), "", "", person.getLastName()+ " moved to Utrecht"));
        notifications.peek((bsn, notification) -> System.out.println("key " + bsn + " notification " + notification));
        notifications.to(Notification.topicName, Produced.with(AppSerdes.String(), AppSerdes.Notification()));
        personsMoved.to(Person.topicName+"_"+newProvince,Produced.with(AppSerdes.String(), AppSerdes.Person()));
        personsMoved.to(Person.topicName,Produced.with(AppSerdes.String(), AppSerdes.Person()));// mind recursion
        
        final Topology topology = builder.build();

        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
