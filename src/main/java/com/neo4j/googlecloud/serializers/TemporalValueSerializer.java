package com.neo4j.googlecloud.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.neo4j.values.storable.TemporalValue;

import java.io.IOException;

public class TemporalValueSerializer extends StdSerializer<TemporalValue> {
    public TemporalValueSerializer() {
        this(null);
    }

    public TemporalValueSerializer(Class<TemporalValue> t) {
        super(t);
    }

    @Override
    public void serialize(
            TemporalValue dt, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeString(dt.prettyPrint());
    }
}
