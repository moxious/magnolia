package com.neo4j.magnolia.polyglot;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.*;

import java.util.concurrent.Callable;

public class Test {
    public static class MyClass {
        public int               id    = 42;
        public String            text  = "42";
        public int[]             arr   = new int[]{1, 42, 3};
        public Callable<Integer> gimme = () -> 42;
    }

    public static void main(String [] args) throws Exception {
        ExternalFn efJS = new ExternalFn("first", "first.js", "js");
        ExternalFn efPY = new ExternalFn("second", "second.py", "python");

        efJS.invoke(new MyClass(), null, null);
        efPY.invoke(new MyClass(), null, null);
    }
}
