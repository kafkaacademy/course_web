package academy.kafka;

import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import academy.kafka.config.AppConfig;

/**
 * see
 * https://kafka.apache.org/26/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
 * also interesting
 * :https://www.cloudkarafka.com/blog/2019-04-10-apache-kafka-idempotent-producer-avoiding-message-duplication.html
 */
public class IdemPotent00TransactionAwareProducer {
        static public final String topic = "idempotent_test";

        public static void main(String[] args) {
                academy.kafka.utils.KafkaUtils.createTopic(topic, 1, 1);
                // academy.kafka.utils.KafkaUtils.createTopic(topic, 3, 3);

                Properties props = new Properties();
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                                org.apache.kafka.common.serialization.StringSerializer.class);
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                                org.apache.kafka.common.serialization.StringSerializer.class);
                props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
                props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "producer-1");// unique for this producer (also when
                                                                                // restarting)
                Producer<String, String> producer = new KafkaProducer<>(props);
                producer.initTransactions();// to initialize transactions
                try {
                        producer.beginTransaction();

                        for (int i = 0; i < 100; i++) {
                                producer.send(new ProducerRecord<String, String>(topic, Integer.toString(i),
                                                Integer.toString(i)));
                                System.out.printf("produced %d\n", i);
                                if (i == 10){
                                        producer.commitTransaction();
                                        producer.beginTransaction();
                                }
                                if (i == 20){
                                         throw new Exception("force stop");
                                }
                        }
                        producer.commitTransaction();

                      
                } catch (Exception exception) {
                        producer.abortTransaction();
                } finally {
                        producer.close();
                }
        }
}
