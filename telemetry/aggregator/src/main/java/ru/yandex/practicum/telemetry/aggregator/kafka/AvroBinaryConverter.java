package ru.yandex.practicum.telemetry.aggregator.kafka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

@Component
public class AvroBinaryConverter {

    public <T extends SpecificRecordBase> byte[] toBytes(T value) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            new SpecificDatumWriter<T>(value.getSchema()).write(value, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to serialize Avro message", exception);
        }
    }
}
