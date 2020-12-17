package academy.kafka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;

public class AvroUtils {

    public static byte[] writeGenericBinary(org.apache.avro.generic.GenericData.Record[] records,
            org.apache.avro.Schema schema) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GenericDatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        for (int i = 0; i < records.length; i++) {
            org.apache.avro.generic.GenericData.Record record = records[i];
            datumWriter.write(record, encoder);
        }
        encoder.flush();
        return outputStream.toByteArray();
    }

    public static List<org.apache.avro.generic.GenericRecord> readGenericBinary(byte[] bytes,
            org.apache.avro.Schema schema) throws IOException {
        GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
        SeekableByteArrayInput inputStream = new SeekableByteArrayInput(bytes);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(inputStream, null);
        List<GenericRecord> result = new ArrayList<org.apache.avro.generic.GenericRecord>();
        while (!decoder.isEnd()) {
            GenericRecord item = datumReader.read(null, decoder);
            result.add(item);
        }
        return result;
    }

    public static byte[] writeSpecificData(SpecificRecord[] records, Schema schema) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ReflectDatumWriter<SpecificRecord> datumWriter = new ReflectDatumWriter<SpecificRecord>(schema);
        DataFileWriter<SpecificRecord> dataFileWriter = new DataFileWriter<SpecificRecord>(datumWriter);
        dataFileWriter.create(schema, outputStream);
        for (SpecificRecord record: records) {
            dataFileWriter.append(record);
        }
        dataFileWriter.flush();
        dataFileWriter.close();
        return outputStream.toByteArray();
    }

    public static byte[] writeSpecificData(SpecificRecordBase record, Schema schema) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ReflectDatumWriter<SpecificRecord> datumWriter = new ReflectDatumWriter<>(schema);
        DataFileWriter<SpecificRecord> dataFileWriter = new DataFileWriter<SpecificRecord>(datumWriter);
        dataFileWriter.create(schema, outputStream);
        dataFileWriter.append(record);
        dataFileWriter.flush();
        dataFileWriter.close();
        return outputStream.toByteArray();
    }

    public static List<SpecificRecord> readSpecificData(byte[] bytes, Schema schema) throws IOException {
        SpecificDatumReader<SpecificRecord> datumReader = new SpecificDatumReader<SpecificRecord>(schema);
        SeekableByteArrayInput inputStream = new SeekableByteArrayInput(bytes);
        DataFileReader<SpecificRecord> dataFileReader = new DataFileReader<SpecificRecord>(inputStream, datumReader);
        List<SpecificRecord> list = new ArrayList<SpecificRecord>();
        Iterator<SpecificRecord> it = dataFileReader.iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        dataFileReader.close();
        return list;
    }

    public static SpecificRecord readSpecificDataOneRecord(byte[] bytes, Schema schema) throws IOException {
        SpecificDatumReader<SpecificRecord> datumReader = new SpecificDatumReader<SpecificRecord>(schema);
        SeekableByteArrayInput inputStream = new SeekableByteArrayInput(bytes);
        DataFileReader<SpecificRecord> dataFileReader = new DataFileReader<>(inputStream, datumReader);
        Iterator<SpecificRecord> it = dataFileReader.iterator();
        SpecificRecord result = null;
        if (it.hasNext())
            result = it.next();
        dataFileReader.close();
        return result;
    }

    public static Schema getSchema(SpecificRecord record) {
        return record.getSchema();

    }
}
