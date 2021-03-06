#!/bin/bash
# Simple shell script to start a local docker db which demonstrates this code.
docker stop neo4j-empty

PASSWORD=admin
CWD=`pwd`
NEO4J=neo4j-graal:3.3.5-enterprise

rm -rf target/original-*.jar target/magnolia
mkdir target/magnolia
cp -r magnolia-examples/* target/magnolia

docker run --rm --name neo4j-empty --rm \
	-p 127.0.0.1:7474:7474 \
        -p 127.0.0.1:7687:7687 \
        --env=NEO4J_dbms_memory_pagecache_size=1G \
        --env=NEO4J_dbms_memory_heap_initial__size=2G \
        --env=NEO4J_dbms_memory_heap_max__size=4G \
	--env=NEO4J_AUTH=neo4j/admin \
	--env=NEO4J_ACCEPT_LICENSE_AGREEMENT=yes \
        -v `pwd`/target:/plugins \
        -v $HOME/.google:/google \
        --env=NEO4J_dbms_security_procedures_unrestricted="magnolia.*,google.*,com.neo4j.magnolia.*" \
        --env=NEO4J_magnolia_configuration=/plugins/magnolia/magnolia.yaml \
        --env=NEO4J_magnolia_pubsub_project=testbed-187316" \
        --env=NEO4J_magnolia_pubsub_topic=tmp" \
        --env=GOOGLE_APPLICATION_CREDENTIALS=/google/testbed-187316-a4da62423f60.json \
	-t $NEO4J

#echo "When you're ready to run some cypher, execute this:"
#echo docker exec --interactive --tty neo4j-empty bin/cypher-shell -a localhost -u neo4j -p $PASSWORD
