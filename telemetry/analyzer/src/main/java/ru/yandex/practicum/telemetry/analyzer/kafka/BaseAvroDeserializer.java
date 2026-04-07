package ru.yandex.practicum.telemetry.analyzer.kafka;

import java.io.IOException;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;

public abstract class BaseAvroDeserializer<T extends SpecificRecordBase> {

    private final SpecificDatumReader<T> reader;

    protected BaseAvroDeserializer(Class<T> targetType) {
        this.reader = new SpecificDatumReader<>(targetType);
    }

    public T deserialize(byte[] data) {
        try {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to deserialize avro message", exception);
        }
    }
}
