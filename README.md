Magnolia
===========

Neo4j server plugin for streaming node and edge messages to pub/sub
connectors, and other utilities useful for testing.

1. Build it:

        mvn clean package -DskipTests

2. Copy target/magnolia*.jar to the plugins/ directory of your Neo4j server.

3.  Configure your Neo4j server:

```
pubsub.provider=google
pubsub.project=my-google-project-id
pubsub.topic=some-google-pubsub-topic-id
```

4. Start your Neo4j Server

5. Run any sample cypher to create some data:

```
CREATE (p1:Person { name: "David" }),
(p2:Person { name: "Mark" }),
(p3:Person { name: "Susan" })

MERGE (p1)-[:KNOWS]->(p2)
MERGE (p2)-[:KNOWS]->(p3)
MERGE (p3)-[:KNOWS]->(p2);
```

6. If you have an existing pull subscription on the topic, you should see messages published.  Messages look like this:

Nodes:
```
{ 
  "entityType":"node",
  "id":0,
  "event":"create",
  "properties":{"name":"David"},
  "labels":["Person"]
}
```

Relationships:
```
{
  "entityType":"relationship",
  "start":2,
  "end":1,
  "id":2,
  "event":"create",
  "type":"KNOWS",
  "properties":{}
}
```
