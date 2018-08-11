package com.neo4j.googlecloud.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeSerializer extends StdSerializer<Node> {
    public NodeSerializer() {
        this(null);
    }

    public NodeSerializer(Class<Node> t) {
        super(t);
    }

    @Override
    public void serialize(
            Node n, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        List<String> labels = new ArrayList<String>();
        for (Label l : n.getLabels()) { labels.add(l.name()); }

        jgen.writeObjectField("id", n.getId());
        jgen.writeObjectField("entityType", "node");
        jgen.writeObjectField("labels", labels);
        jgen.writeObjectField("properties", n.getAllProperties());

        jgen.writeEndObject();
    }
}
