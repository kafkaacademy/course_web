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

import academy.kafka.config.AppConfig;
import academy.kafka.entities.Car;
import academy.kafka.serdes.AppSerdes;

public class Stream03ToTopic {
    static Random rn = new Random();// helper, remove in production

    public static void main(final String[] args) {
    
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamFilter" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, Car> cars = builder.stream(Car.topicName, Consumed.with(AppSerdes.String(), AppSerdes.Car()));
          cars.filter((kenteken, car) -> car.getMake().equals("Acura")).to("acuraCars"); //this topic can be used by other stream apps

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
