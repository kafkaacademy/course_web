package academy.kafka;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueStore;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.Payment;
import academy.kafka.entities.PaymentIntake;
import academy.kafka.entities.PaymentIntakeAggregate;
import academy.kafka.serdes.AppSerdes;

/**
 * Stream aggregates work on streams like transaction logs , every record keeps
 * it's value, like bank records
 * 
 * Exercises : 1. Make this ready to store the data in topic payment 2. Make
 * another streaming app that sends payments Rejected to topic
 * FunctionalException
 * 
 */
public class IdemPotent07Show {
    static Random rn = new Random();// helper, remove in production

    public static void main(final String[] args) {
        System.out.println("...please wait till I am ready with building up the table");

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamFilter" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Payment> payments = builder.stream(Payment.topicName,
                Consumed.with(AppSerdes.String(), AppSerdes.Payment()));
                payments.filter((k,v)-> v.getStatus()==Payment.Status.ACCEPTED).peek((k,v)->System.out.println("Accepted:"+v));
                payments.filter((k,v)-> v.getStatus()==Payment.Status.REJECTED).peek((k,v)->System.out.println("Rejected:"+v));
     
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
