package com.neo4j.googlecloud.pubsub.userfn;

import com.neo4j.googlecloud.pubsub.AbstractPubsubTest;
import org.junit.Test;
import org.neo4j.driver.v1.*;

import java.util.Map;

import static org.junit.Assert.*;
import static org.neo4j.driver.v1.Values.parameters;

public class UserFunctionsTest extends AbstractPubsubTest {
    private static String project = "testbed-187316";
    private static String topic = "tmp";

    @Test
    public void providesVersion() {
        String version = s.readTransaction(new TransactionWork<String>() {
            @Override
            public String execute(Transaction tx) {
                StatementResult result = tx.run("RETURN google.pubsub.version();",
                        parameters());
                return result.single().get(0).asString();
            }
        });

        assertEquals(version, UserFunctions.version);
    }

    @Test public void publishMessage() {
        String q = "RETURN google.pubsub.publish.message('" + project + "', '" + topic + "', " +
                "{foo:'bar', x: 1});";

        StatementResult r = runQuery(q);
        System.out.println(r.single().get(0));
    }

    public boolean isValidResponse(Map<String,Object> result) {
        assertNotNull(result);
        assertTrue(result.containsKey("cancelled"));
        assertTrue(result.containsKey("done"));
        return true;
    }

    @Test public void publishNode() throws InterruptedException {
        String q = "MATCH (p:Person { name: 'David' }) RETURN google.pubsub.publish.node(p) LIMIT 1;";
        StatementResult r = runQuery(q);
        isValidResponse(r.single().get(0).asMap());
    }

    @Test public void publishRelationship() {
        final String knowsQuery = "MATCH (p1:Person)-[r:KNOWS]->(p2:Person) RETURN google.pubsub.publish.relationship(r) LIMIT 1;";
        StatementResult r = runQuery(knowsQuery);
        isValidResponse(r.single().get(0).asMap());
    }

    @Test public void publishPath() {
        final String knowsQuery = "MATCH p=(p1:Person)-[r:KNOWS]->(p2:Person) RETURN google.pubsub.publish.path(p) LIMIT 1;";
        StatementResult r = runQuery(knowsQuery);
        isValidResponse(r.single().get(0).asMap());
    }

    @Test public void publishQuery() {
        final String knowsQuery = "RETURN google.pubsub.publish.query('MATCH (p1:Person)-[r:KNOWS]->(p2:Person) RETURN p1, r, p2 LIMIT 1');";
        StatementResult res = runQuery(knowsQuery);

        // While the inner query has many results, the outer query (publish.query) just returns number of records.
        assertEquals(res.single().get(0).asInt(), 1);
    }
}