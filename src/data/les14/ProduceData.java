package academy.kafka;

import academy.kafka.entities.Car;
import academy.kafka.entities.Day;
import academy.kafka.entities.FunctionalException;
import academy.kafka.entities.PaymentRequest;
import academy.kafka.entities.Person;
import academy.kafka.entities.Registration;


public class ProduceData {

    public static void main(final String[] args) {
        academy.kafka.utils.KafkaUtils.deleteTopic(Car.topicName);
        academy.kafka.utils.KafkaUtils.createTopic(Car.topicName, 1, 1);
        academy.kafka.utils.KafkaUtils.deleteTopic(Person.topicName);
        academy.kafka.utils.KafkaUtils.createTopic(Person.topicName, 1, 1);
        academy.kafka.utils.KafkaUtils.deleteTopic(Registration.topicName);
        academy.kafka.utils.KafkaUtils.createTopic(Registration.topicName, 1, 1);
        academy.kafka.utils.KafkaUtils.deleteTopic(PaymentRequest.topicName);
        academy.kafka.utils.KafkaUtils.createTopic(PaymentRequest.topicName, 1, 1);

        academy.kafka.utils.KafkaUtils.deleteTopic(FunctionalException.topicName);
        academy.kafka.utils.KafkaUtils.createTopic(FunctionalException.topicName, 1, 1);

        academy.kafka.GenerateData.generateSeriousStartPoint(10);

        academy.kafka.utils.KafkaUtils.deleteTopic(Day.topicName);
        academy.kafka.utils.KafkaUtils.createTopic(Day.topicName, 1, 1);
       // new UpdateDay(0,2009).start();
    }
}
