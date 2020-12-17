package academy.kafka.serializers.examples;

import academy.kafka.config.AppConfig;
import academy.kafka.serializers.AvroSpecificDeserializer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
public final class AvroConsumer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("schema", AvroPerson.getClassSchema());
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        props = ConsumerConfig.addDeserializerToConfig(props, new StringDeserializer(), new AvroSpecificDeserializer<AvroPerson>());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "avro_person_group");

        Consumer<String, AvroPerson> consumer = new KafkaConsumer<>(props);

        try {
            consumer.subscribe(Collections.singletonList("avro_persons"));
            while (true) {
                final ConsumerRecords<String, AvroPerson> consumerRecords = consumer.poll(Duration.ofMillis(100));

                consumerRecords.forEach(record -> {
                    AvroPerson person=  record.value();
                    System.out.printf("%s: %s\n", record.key(), person);
                });
                consumer.commitAsync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        consumer.close();

    }
}
