package academy.kafka;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import academy.kafka.entities.Car;
import academy.kafka.entities.FunctionalException;
import academy.kafka.entities.Payment;
import academy.kafka.entities.PaymentRequest;
import academy.kafka.entities.Person;
import academy.kafka.entities.Registration;
import academy.kafka.utils.KafkaUtils;

public final class ShowData {
    static final Random rn = new Random();// helper, remove in production
    static String grpName = "les21Group" + rn.nextInt(10000);
    static final Logger logger = LoggerFactory.getLogger(ShowData.class);

    enum TOPIC {
        Car, Person, Registration, PaymentRequest,Payment, FunctionalException
    }

    public static void main(String[] args) {
        TOPIC tp = TOPIC.Payment;
        switch (tp) {
            case FunctionalException:
                KafkaUtils.<FunctionalException>consumeEntities(FunctionalException.topicName,
                        FunctionalException.class, grpName);
                break;
            case Car:
                KafkaUtils.<Car>consumeEntities(Car.topicName, Car.class, grpName);
                break;
            case Person:
                KafkaUtils.<Car>consumeEntities(Person.topicName, Person.class, grpName);
                break;
            case Payment:
                KafkaUtils.<Payment>consumeEntities(Payment.topicName, Payment.class, grpName);
                break;
            case PaymentRequest:
                KafkaUtils.<PaymentRequest>consumeEntities(PaymentRequest.topicName, PaymentRequest.class, grpName);
                break;
            case Registration:
                KafkaUtils.<Registration>consumeEntities(Registration.topicName, Registration.class, grpName);

        }

    }
}
