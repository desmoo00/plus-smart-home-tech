package ru.yandex.practicum.telemetry.collector.kafka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

@Component
public class AvroBinaryConverter {

    public byte[] toBytes(SpecificRecord record) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            SpecificDatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>(record.getSchema());
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            writer.write(record, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not convert Avro record to byte array", exception);
        }
    }
}
