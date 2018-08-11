package com.neo4j.googlecloud.pubsub;

public enum Neo4jPubsubEventType {
    CREATE {
        public String toString() { return "create"; }
    },

    DELETE {
        public String toString() { return "delete"; }
    },

    NOTICE {
        public String toString() { return "notice"; }
    }
}
