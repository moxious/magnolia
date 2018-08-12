package com.neo4j.magnolia.polyglot;

import org.graalvm.polyglot.Value;

import java.util.*;

public class ValueAdapter {
    public static final List<String> blacklist = Arrays.asList("class", "constructor", "caller", "prototype", "__proto__");

    /**
     * Graal VM polyglot values can't be converted to Neo4j AnyValues, so this is a shim converter to cover a number
     * of cases.
     * @param v a graalvm value
     * @return an object that can be converted to a Neo4j AnyValue.
     */
    public static Object convert(Value v) {
        if (v == null || v.isNull()) return null;
        else if (v.isProxyObject()) {
            System.err.println("Warning: proxy objects are not yet supported from guest languages for neo4j serialization");
            return null;
        }

        Set<String> memberKeys = v.getMemberKeys();


        // Nested map case.
        if (!memberKeys.isEmpty()) {
            Map<String,Object> result = new HashMap<>();

            for(String key : memberKeys) {
                if (!blacklist.contains(key) && !v.getMember(key).canExecute()) {
                    System.out.println("Recursing on " + key);
                    result.put(key, convert(v.getMember(key)));
                }
            }

            return result;
        }

        if (v.isBoolean()) return v.asBoolean();
        if (v.isNumber()) return v.asFloat();
        if (v.isString()) return v.asString();

        System.err.println("Unsupported guest language values cannot be mapped, and will be returned as null");
        return null;
    }
}
