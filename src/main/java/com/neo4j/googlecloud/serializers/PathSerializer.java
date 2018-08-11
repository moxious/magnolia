package com.neo4j.googlecloud.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;

import java.io.IOException;
import java.util.Iterator;

public class PathSerializer extends StdSerializer<Path> {
    public PathSerializer() {
        this(null);
    }

    public PathSerializer(Class<Path> t) {
        super(t);
    }

    @Override
    public void serialize(
            Path p, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();

        jgen.writeObjectField("entityType", "path");
        jgen.writeObjectField("length", p.length());

        // Path is a JSON array of path elements to preserve ordering and
        // avoid pointer chasing.
        jgen.writeArrayFieldStart("path");

        Iterator<PropertyContainer> it = p.iterator();
        while (it.hasNext()) {
            jgen.writeObject(it.next());
        }

        jgen.writeEndArray();

        // Close outer path object.
        jgen.writeEndObject();
    }
}
