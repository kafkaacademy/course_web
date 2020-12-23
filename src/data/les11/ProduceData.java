package academy.kafka;

import academy.kafka.entities.FunctionalException;
import academy.kafka.entities.PaymentIntake;

public class ProduceData {

    public static void main(final String[] args) {
        academy.kafka.utils.KafkaUtils.deleteTopic(PaymentIntake.topicName);
        academy.kafka.utils.KafkaUtils.createTopic(PaymentIntake.topicName, 1, 1);
        academy.kafka.utils.KafkaUtils.deleteTopic(FunctionalException.topicName);
        academy.kafka.utils.KafkaUtils.createTopic(FunctionalException.topicName, 1, 1);

        academy.kafka.GenerateData.generateSandbox(10);
        academy.kafka.GenerateData.createStupidPayments(3);
        academy.kafka.GenerateData.createDoublePaymentIntakes(10, 50); // make 10 payments with  +/- 50% double payments

    }
}
