package academy.kafka;

import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;

import org.apache.kafka.clients.producer.ProducerRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import academy.kafka.config.AppConfig;

public final class SimpleProducer {  
 
    static final Logger logger = LoggerFactory.getLogger(SimpleProducer.class);
    static final public String topic="les03-topic";
      public static void main(String[] args) {
       
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,AppConfig.BootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class);
       
        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 100; i++){
            producer.send(new ProducerRecord<String, String>(topic, Integer.toString(i), Integer.toString(i)));
            System.out.printf("produced %d\n",i);
        }
        producer.close();
        
    } 
}
