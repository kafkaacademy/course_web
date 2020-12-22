package academy.kafka;


public class ProduceData {

    public static void main(final String[] args) {
     
      academy.kafka.GenerateData.generateSandbox(10);
      academy.kafka.GenerateData.createStupidPayments(3);
    }
}
