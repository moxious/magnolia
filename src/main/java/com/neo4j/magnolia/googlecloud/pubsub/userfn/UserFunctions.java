package com.neo4j.googlecloud.pubsub.userfn;

import com.neo4j.googlecloud.pubsub.Neo4jPubsubEventType;
import com.neo4j.googlecloud.pubsub.PubSubConnector;
import com.neo4j.googlecloud.pubsub.PubSubConnectorPool;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class UserFunctions {
    @Context public GraphDatabaseService db;
    @Context public Log log;

    public static final String version = "0.0.1-SNAPSHOT";

    @UserFunction("google.pubsub.version")
    @Description("RETURN google.pubsub.version() | return the current pubsub installed version")
    public String version() {
        return version;
    }

    @UserFunction("google.pubsub.publish.message")
    @Description("RETURN google.pubsub.publish.message('my-project-id', 'pubsub-topic-id', { field1: 'value', field2: value })")
    public Map<String,Object> publishMessage(
            @Name("project") final String project,
            @Name("topic") final String topic,
            @Name("message") final Map<String,Object> message) throws IOException {

        System.out.println("About to publish " + project + "/" + topic + "/" + message);

        try {
            return PubSubConnectorPool.active.get(project, topic).sendMessage(message);
        } catch(Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

    @UserFunction("google.pubsub.publish.query")
    @Description("RETURN pubsub.publish.query('MATCH (p:Person { name: 'Emil' }) RETURN p'")
    public long publishQuery(
            @Name("query") final String query) throws IOException {
        long c = 0;

        PubSubConnector connector = PubSubConnectorPool.active.getDefault();
        Result r = db.execute(query, Collections.emptyMap());

        while(r.hasNext()) {
            Map<String,Object> row = r.next();
            connector.sendMessage(row);
            c++;
        }

        r.close();
        return c;
    }

    @UserFunction("google.pubsub.publish.node")
    @Description("MATCH (n:Person) RETURN google.pubsub.publish.node(n);")
    public Map<String,Object> publishNode(@Name("node") final Node n) throws IOException {
        return PubSubConnectorPool.active.getDefault().send(n, Neo4jPubsubEventType.NOTICE);
    }

    @UserFunction("google.pubsub.publish.relationship")
    @Description("MATCH (n:Person) RETURN google.pubsub.publish.relationship(r);")
    public Map<String,Object> publishRel(@Name("relationship") final Relationship relationship) throws IOException {
        return PubSubConnectorPool.active.getDefault().send(relationship, Neo4jPubsubEventType.NOTICE);
    }

    @UserFunction("google.pubsub.publish.path")
    @Description("MATCH (n:Person) RETURN google.pubsub.publish.path(p);")
    public Map<String,Object> publishPath(@Name("path") final Path path) throws IOException {
        return PubSubConnectorPool.active.getDefault().send(path, Neo4jPubsubEventType.NOTICE);
    }
}
