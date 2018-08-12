Magnolia
===========

## Setup

- Download the latest community graalvm, and unzip it in this directory.
- (Optional) Run `./graalvm-ce-1.0.0-rc5/Contents/Home/bin/gu install python` to install python support.
- Configure your IDE to use GraalVM in that path as the JDK for building this project.
- Build the neo4j-graal docker VM locally:

    cd neo4j-graal
    docker build -t neo4j-graal:3.3.5-enterprise -f Dockerfile .

## Configure Magnolia

You can examine the `create-empty-db.sh` script for examples of how to run with a configuration.

Write a `magnolia.yaml` file that looks like this:

    basePath: "/plugins/magnolia"
    procedures:
      - name: my-proc
        file: my-proc.js
        language: js
    functions:
      - name: my-function
        file: my-function.js
        language: js

The file `my-fuction.js` will be expected to be found under `basePath`.

Set the magnolia configuration in neo4j by using the following in neo4j.conf:

`magnolia.configuration=/path/to/magnolia.yaml`

The purpose of setting configuration to be in a separate yaml file, and not entirely in neo4j.conf
as is normal with other plugins, is to allow dynamic reloading.  In this way, you can change magnolia.yaml, and 
even add functions, without restarting neo4j.

**Recommended**: create a directory under the plugins directory where you install neo4j, and put all of your
extension scripts, and magnolia.yaml file there.  Ensure everything magnolia needs is under the basePath
specified by 

## Examples of Using Magnolia

### List Configured Functions

The `list()` procedure returns the available registered dynamic functions & procedures.

    neo4j> call com.neo4j.magnolia.polyglot.list();
    +-----------------------------------------------------------------+
    | name             | file                | language | type        |
    +-----------------------------------------------------------------+
    | "make-node"      | "make-node.js"      | "js"     | "procedure" |
    | "demo-neo4j-api" | "demo-neo4j-api.js" | "js"     | "function"  |
    | "echo"           | "echo.js"           | "js"     | "function"  |
    | "first"          | "first.js"          | "js"     | "function"  |
    | "second"         | "second.py"         | "python" | "function"  |
    +-----------------------------------------------------------------+
    
    5 rows available after 167 ms, consumed after another 7 ms

### Call a Registered Function

The `magnolia.fn` cypher function takes two arguments; the name of the dynamic function you want to 
execute, and an argument to that function (which may be a map or other neo4j structure)

    neo4j>     CREATE (p:Person { name: "Emil" })
               RETURN magnolia.fn('echo', "Hello, " + p.name) as greeting;
    +---------------+
    | greeting      |
    +---------------+
    | "Hello, Emil" |
    +---------------+
    
    1 row available after 77 ms, consumed after another 0 ms
    Added 1 nodes, Set 1 properties, Added 1 labels

### Call Dynamic Function

The `magnolia.dynamic` function allows you to put javascript directly into cypher.  In the function
example below, "Interop.import" is how GraalVM takes arguments from the guest language, and the final
argument "5" is what's bound to "arguments" inside of the javascript code.  The first argument ("js") simply
indicates that the code is intended as javascript.

    neo4j> WITH 'const input = Interop.import("arguments"); input * input' AS javascriptSquareInput
           RETURN magnolia.dynamic('js', javascriptSquareInput, 10);
    +---------------------------------------------------+
    | magnolia.dynamic('js', javascriptSquareInput, 10) |
    +---------------------------------------------------+
    | 100.0                                             |
    +---------------------------------------------------+

## PubSub

Magnolia also contains a Neo4j server plugin for streaming node and edge messages to pub/sub
connectors, and other utilities useful for testing.

1. Build it:

        mvn clean package -DskipTests

2. Copy target/magnolia*.jar to the plugins/ directory of your Neo4j server.

3.  Configure your Neo4j server:

```
magnolia.pubsub.project=my-google-project-id
magnolia.pubsub.topic=some-google-pubsub-topic-id
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
