package com.neo4j.googlecloud.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.neo4j.values.storable.DateValue;

import java.io.IOException;

public class DateValueSerializer extends StdSerializer<DateValue> {
    public DateValueSerializer() {
        this(null);
    }

    public DateValueSerializer(Class<DateValue> t) {
        super(t);
    }

    @Override
    public void serialize(
            DateValue dt, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeString(dt.prettyPrint());
    }
}
