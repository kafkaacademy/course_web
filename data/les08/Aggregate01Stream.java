package academy.kafka;

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
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.state.KeyValueStore;

import academy.kafka.config.AppConfig;
import academy.kafka.entities.PaymentRequest;
import academy.kafka.entities.ProvinceAggregate;
import academy.kafka.serdes.AppSerdes;

public class Aggregate01Stream {
    static Random rn = new Random();// helper, remove in production

    public static void main(final String[] args) {
        System.out.println("...please wait till I am ready with building up the table");

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamFilter" + rn.nextInt(10000));
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());
        StreamsBuilder builder = new StreamsBuilder();
   
   /**here step by step done */
        KStream<String, PaymentRequest> stream = builder.stream(PaymentRequest.topicName,
                Consumed.with(AppSerdes.String(), AppSerdes.PaymentRequest()));
                /**first we group the stream on some key */
        KGroupedStream<String, PaymentRequest> grped = stream.groupBy((k, v) -> v.getProvinceName(),
                Grouped.with(AppSerdes.String(), AppSerdes.PaymentRequest()));
       /**
        * agregating is on already grouped objects
          grouped objects consists of a key and a substream , the aggregating is on the substream
          the result is always a table
          input records with null values in the substream are neglected 
          (in the table aggregate this is different they are seen as delete's)
        */
        KTable<String, ProvinceAggregate> tbl = grped.aggregate(() -> new ProvinceAggregate(null, 0) 
                , (k, v, aggValue) -> new ProvinceAggregate(v.getProvinceName(), aggValue.getTotal() + v.getAmount().intValue()),
                Materialized.<String, ProvinceAggregate, KeyValueStore<Bytes, byte[]>>as("provinceTotalStoreName")
                        .withKeySerde(AppSerdes.String()).withValueSerde(AppSerdes.ProvinceAggregate()));
        /* A ktable can not be printed , you have to make a stream of it again */       
                        KStream<String, ProvinceAggregate> streamAgain = tbl.toStream();

        streamAgain.print(Printed.<String, ProvinceAggregate>toSysOut().withLabel("province totals"));

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
