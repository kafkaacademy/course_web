package academy.kafka;

import academy.kafka.serializers.examples.AvroPerson;
import java.io.IOException;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;

public class Main {

    public static void main(String args[]) throws IOException {

        Schema schema = AvroPerson.getClassSchema();
        AvroPerson avroPerson1 = new AvroPerson("BSN_Puk", "Pietje", "Puk", "IBAN_Pietje");
        AvroPerson avroPerson2 = new AvroPerson("BSN_Doe", "John", "Doe", "IBAN_John");
        AvroPerson[] array = { avroPerson1, avroPerson2 };
        byte[] data = AvroUtils.writeSpecificData(array, schema);
        List<SpecificRecord> records = AvroUtils.readSpecificData(data, schema);
        for (SpecificRecord record : records)
            System.out.println(record);
    }
}
