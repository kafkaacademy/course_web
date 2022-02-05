package academy.kafka;

import java.util.Properties;
import java.util.Random;

import java.time.Duration;
import java.util.Collections;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import academy.kafka.config.AppConfig;

/**
 * Note, that there is nothing transactional about the above code. But, since we
 * used read_committed, it means that no messages that were written to the input
 * topic in the same transaction will be read by this consumer until they are
 * all written. see also
 * 
 * https://www.cloudkarafka.com/blog/2019-04-10-apache-kafka-idempotent-producer-avoiding-message-duplication.html
 */
public class IdemPotent00TransactionAwareConsumer {
        static final Random rn = new Random();// helper, remove in production
        static public final String topic = IdemPotent00TransactionAwareProducer.topic;

        public static void main(String[] args) {
                Properties props = new Properties();
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                                org.apache.kafka.common.serialization.StringDeserializer.class);
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                                org.apache.kafka.common.serialization.StringDeserializer.class);
                props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group-id");//not random anymore!!
                props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");//because read_committed
                props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
                Consumer<String, String> consumer = new KafkaConsumer<>(props);
                consumer.subscribe(Collections.singletonList(topic));

                try {

                        while (true) {
                                final ConsumerRecords<String, String> consumerRecords = consumer
                                                .poll(Duration.ofMillis(100));

                                consumerRecords.forEach(record -> {
                                        System.out.printf("Consumer Record:(key=%s, value=%s, partition=%s, %s)\n",
                                                        record.key(), record.value(), record.partition(),
                                                        record.offset());
                                });
                                consumer.commitAsync();
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
                System.out.println("consumer stopped");
                consumer.close();
        }
}
