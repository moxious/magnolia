package com.neo4j.googlecloud.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.neo4j.values.storable.DateTimeValue;

import java.io.IOException;

public class DateTimeValueSerializer extends StdSerializer<DateTimeValue> {
    public DateTimeValueSerializer() {
        this(null);
    }

    public DateTimeValueSerializer(Class<DateTimeValue> t) {
        super(t);
    }

    @Override
    public void serialize(
            DateTimeValue dt, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeString(dt.prettyPrint());
    }
}
