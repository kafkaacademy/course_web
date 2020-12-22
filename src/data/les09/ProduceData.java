package academy.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import academy.kafka.config.AppConfig;

public class ProduceData {

    public static void main(final String[] args) throws Exception {
     
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.BootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);

        String[] words = { "org", "apache", "kafka" };
        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int j = 0; j < 100; j++) {
            for (int i = 0; i < 3; i++) {
                producer.send(new ProducerRecord<String, String>("TextLinesTopic", Integer.toString(i), words[i]));
                System.out.printf("produced %s\n", words[i]);
                Thread.sleep(2000);        
            }
            Thread.sleep(2000);
        }
        producer.close();
    }
}
