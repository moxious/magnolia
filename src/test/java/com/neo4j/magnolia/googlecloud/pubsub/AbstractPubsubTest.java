package com.neo4j.googlecloud.pubsub;

import org.junit.*;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.junit.Neo4jRule;

public class AbstractPubsubTest {
    public static final String project = "testbed-187316";
    public static final String topic = "tmp";
    public Session s;
    public static final String cypherFixture = new StringBuilder()
            .append("CREATE (:Person { name: 'David' })-[:KNOWS]->(:Person { name: 'John' });")
            .toString();

    @ClassRule
    public static Neo4jRule neo4j = new Neo4jRule()
            .withConfig("dbms.security.procedures.unrestricted","google.pubsub.*")
            .withConfig("google.pubsub.topic", topic)
            .withConfig("google.pubsub.project", project)
            .withFixture(cypherFixture)
            .withFunction(com.neo4j.googlecloud.pubsub.userfn.UserFunctions.class);
    protected static Driver driver;

    @BeforeClass
    public static void setup() {
        driver = GraphDatabase.driver( neo4j.boltURI(), AuthTokens.basic( "neo4j", "neo4j"  ),
                Config.build().withoutEncryption().toConfig() );
    }

    @Before public void setupSession() {
        s = driver.session();
    }

    @After public void teardownSession() {
        s.close();
    }

    @AfterClass
    public static void teardown() {
        driver.close();
    }

    public StatementResult runQuery(final String query) {
        return s.writeTransaction(tx -> tx.run(query));
    }
}
