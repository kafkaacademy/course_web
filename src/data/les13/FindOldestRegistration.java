package academy.kafka;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.Day;
import academy.kafka.entities.Registration;
import academy.kafka.serdes.AppSerdes;

/**
 * find oldest registration
 */
public class FindOldestRegistration {
    static Random rn = new Random();// helper, remove in production

    public static void main(final String[] args) {
        System.out.println("...please wait till I am ready with building up the table");

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamFilter" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Registration> registrations = builder.stream(Registration.topicName,
                Consumed.with(AppSerdes.String(), AppSerdes.Registration()));
        
        KStream<String, Registration> registrationsOnFromDate = registrations.selectKey((regid, registration) -> "oldestFromAll");
        registrationsOnFromDate.peek((k,v)-> System.out.println("test "+k+":"+v.getFrom()));
        KGroupedStream<String, Registration> grped = registrationsOnFromDate.groupByKey();
        KTable<String, Registration> tbl = grped.reduce((agrReg, newReg) -> {
            if (newReg.getFrom().isBefore(agrReg.getFrom()))
                return newReg;//is older reg :)
            else
                return agrReg;
        });

        tbl.toStream().peek((k,v)-> System.out.println(k+":"+v.getFrom()));
       
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

    static int oldestYear=2000;
      
    
    public static int  getOldestYear() {
        System.out.println("...please wait till I am ready with building up the table");

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamFilter" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Registration> registrations = builder.stream(Registration.topicName,
                Consumed.with(AppSerdes.String(), AppSerdes.Registration()));
        
        KStream<String, Registration> registrationsOnFromDate = registrations.selectKey((regid, registration) -> "oldestFromAll");
        registrationsOnFromDate.peek((k,v)-> System.out.println("test "+k+":"+v.getFrom()));
        KGroupedStream<String, Registration> grped = registrationsOnFromDate.groupByKey();
        KTable<String, Registration> tbl = grped.reduce((agrReg, newReg) -> {
            if (newReg.getFrom().isBefore(agrReg.getFrom()))
                return newReg;//is oldest reg :)
            else
                return agrReg;
        });

        tbl.toStream().peek((k,v)->{oldestYear=v.getFrom().getYear(); System.out.println(k+":"+v.getFrom());});
       
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
       return oldestYear;
    }
}
