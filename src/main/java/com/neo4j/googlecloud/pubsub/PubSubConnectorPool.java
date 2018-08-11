package com.neo4j.googlecloud.pubsub;

import java.util.concurrent.ConcurrentHashMap;

public class PubSubConnectorPool {
    public static final int maxSize = 111;
    private ConcurrentHashMap<String,PubSubConnector> pool;

    public static final PubSubConnectorPool active = new PubSubConnectorPool();

    public PubSubConnectorPool() {
        pool = new ConcurrentHashMap<>(maxSize);
    }

    private String keyFor(String project, String topic) {
        return project + "/" + topic;
    }

    public PubSubConnector getDefault() {
        String topicId = PubsubConfiguration.get("topic", "UNDEFINED") + "";
        String projectId = PubsubConfiguration.get("project", "UNDEFINED") + "";
        PubSubConnector conn = get(projectId, topicId);
        return conn;
    }

    public PubSubConnector get(String project, String topic) {
        String k = keyFor(project, topic);

        if (pool.containsKey(k)) {
            return pool.get(k);
        }

        return set(project, topic, new PubSubConnector(project, topic));
    }

    public PubSubConnector set(String project, String topic, PubSubConnector conn) {
        pool.put(keyFor(project, topic), conn);
        return conn;
    }
}
