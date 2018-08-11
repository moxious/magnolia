package com.neo4j.googlecloud.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.pubsub.v1.PubsubMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.kernel.impl.logging.LogService;
import org.neo4j.kernel.impl.logging.SimpleLogService;
import org.neo4j.logging.FormattedLogProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PubSubConnectorTest {
    PubSubConnector conn;
    private static String project = "my-project";
    private static String topic = "my-topic";

    @Before
    public void setup() {
        LogService svc = new SimpleLogService(
                FormattedLogProvider.toOutputStream(System.err),
                FormattedLogProvider.toOutputStream(System.err));
        PubSubConnector.initialize(svc);
        conn = new PubSubConnector(project, topic);
    }

    @Test
    public void canDescribe() {
        assertEquals((project + "/" + topic), conn.describe());
    }

    @Test
    public void toMsg() throws IOException {
        Map<String,Object> data = new HashMap<>();
        data.put("foo", "bar");
        data.put("x", 1L);

        PubsubMessage msg = conn.toMsg(data);
        String jsonText = new String(msg.getData().toByteArray());
        System.out.println("JSON TEXT " + jsonText);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode obj = mapper.readTree(jsonText);
        assertNotNull(obj);
        JsonNode timeVal = obj.get("time");
        assertNotNull(timeVal);
        assertTrue(timeVal.isTextual());

        assertEquals(obj.get("foo").textValue(), "bar");
    }
}