
package academy.kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import academy.kafka.config.AppConfig;
import academy.kafka.serdes.AppSerdes;
import academy.kafka.utils.KafkaUtils;

public class WordCountApplication {
  static Random rn = new Random();// helper, remove in production

  public static void main(final String[] args) throws Exception {
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application" + rn.nextInt(10000));
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, AppSerdes.String().getClass());

    StreamsBuilder streamsBuilder = new StreamsBuilder();
    KStream<String, String> textLines = streamsBuilder.stream("TextLinesTopic",
        Consumed.with(AppSerdes.String(), AppSerdes.String()));
    KStream<String, Object> str = textLines
        .flatMapValues(textLine -> Arrays.asList(textLine.toLowerCase().split("\\W+")));
    KGroupedStream<Object, Object> grped = str.groupBy((key, word) -> word);
    KTable<Windowed<Object>, Long> aggregatedStream = grped.windowedBy(TimeWindows.of(Duration.ofMinutes(2))).count();
    aggregatedStream.toStream().peek((key, value)-> System.out.println(" Window start: " + key + " " + KafkaUtils.formatTimestamp(key.window().start())
        + " Window end: " + KafkaUtils.formatTimestamp(key.window().end()) + " Count: " + value));
    KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), config);
    streams.start();

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

}