package com.neo4j.googlecloud.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.neo4j.values.storable.PointValue;

import java.io.IOException;

public class PointValueSerializer extends StdSerializer<PointValue> {
    public PointValueSerializer() {
        this(null);
    }

    public PointValueSerializer(Class<PointValue> t) {
        super(t);
    }

    @Override
    public void serialize(
            PointValue point, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject();
        jgen.writeStringField("crs", point.getCoordinateReferenceSystem().getName());
        jgen.writeArrayFieldStart("coordinate");
        double [] arr = point.getCoordinate().getCoordinate().stream().mapToDouble(Double::doubleValue).toArray();
        for (double d : arr) {
            jgen.writeNumber(d);
        }
        jgen.writeEndArray();
        jgen.writeEndObject();
    }
}
