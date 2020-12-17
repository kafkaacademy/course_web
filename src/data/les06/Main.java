package academy.kafka;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

public class Main {

    public static void main(String args[]) throws IOException {

        AvroPerson avroPerson = new AvroPerson("BSN1235", "Pietje", "Puk", "IBAN1234");

        DatumWriter<AvroPerson> personDatumWriter = new SpecificDatumWriter<AvroPerson>(AvroPerson.class);
        DataFileWriter<AvroPerson> dataFileWriter = new DataFileWriter<AvroPerson>(personDatumWriter);
        dataFileWriter.create(avroPerson.getSchema(), new File("persons.avro"));
        final int numRecords = 30000;
        for (int i = 0; i < numRecords; i++)
            dataFileWriter.append(avroPerson);
        dataFileWriter.close();

        File file = new File("persons.avro");
        DatumReader<AvroPerson> userDatumReader = new SpecificDatumReader<AvroPerson>(AvroPerson.class);
        DataFileReader<AvroPerson> dataFileReader = new DataFileReader<AvroPerson>(file, userDatumReader);
        AvroPerson avroPerson2 = null;
        while (dataFileReader.hasNext()) {
            avroPerson2 = dataFileReader.next(avroPerson2);
            System.out.println(avroPerson2);
        }
        dataFileReader.close();

        Path path = Paths.get("persons.avro");

        try {
            long bytes = Files.size(path);
            System.out.println(String.format("file size : %,d bytes", bytes));
            System.out.println(String.format("average per record :%,d bytes", bytes / numRecords));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
