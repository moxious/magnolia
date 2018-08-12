package com.neo4j.magnolia.polyglot;

import org.graalvm.polyglot.Value;

import java.util.*;

/**
 * Neo4j has a limited set of object it can map to AnyValue for use with cypher.  Weirdo polyglot objects aren't
 * in that list.  So this class contains utilities for mapping those values back to more vanilla Java objects.
 *
 * For example, if JavaScript returns {"foo":"bar"}, that's a javascript hash, not a Map<String,String>().
 */
public class ValueAdapter {
    public static final List<String> blacklist = Arrays.asList("class", "constructor", "caller", "prototype", "__proto__");

    /**
     * @param v a graalvm value
     * @return an object that can be converted to a Neo4j AnyValue.
     */
    public static Object convert(Value v) {
        if (v == null || v.isNull()) return null;
        if (v.isProxyObject()) {
            System.err.println("Warning: proxy objects are not yet supported from guest languages for neo4j serialization");
            return null;
        }

        if (v.isHostObject()) {
            // The host in this case is java; this means the script is trying to return something like a stream,
            // or a node...which should pass through without conversion.  Might still be a serialization error, but
            // that would be the script's fault and not a polyglot translation issue.
            //
            // Graal's polygot options lets java objects (here, "host objects") to be created directly
            // inside of other languages.
            return v.asHostObject();
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

        // Don't have a way to know which type of number it is, so we will always coerce widest
        // for safety.
        if (v.isNumber()) return v.asDouble();

        if (v.isString()) return v.asString();

        System.err.println("Unsupported guest language values cannot be mapped, and will be returned as null");
        return null;
    }
}
