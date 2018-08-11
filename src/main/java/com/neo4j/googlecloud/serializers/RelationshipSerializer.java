package com.neo4j.googlecloud.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;

public class RelationshipSerializer extends StdSerializer<Relationship> {
    public RelationshipSerializer() {
        this(null);
    }

    public RelationshipSerializer(Class<Relationship> t) {
        super(t);
    }

    @Override
    public void serialize(
            Relationship r, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        jgen.writeObjectField("id", r.getId());
        jgen.writeObjectField("start", r.getStartNodeId());
        jgen.writeObjectField("end", r.getEndNodeId());
        jgen.writeObjectField("entityType", "relationship");
        jgen.writeObjectField("type", r.getType().name());
        jgen.writeObjectField("properties", r.getAllProperties());

        jgen.writeEndObject();
    }
}
