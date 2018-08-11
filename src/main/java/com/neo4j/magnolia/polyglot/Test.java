package com.neo4j.magnolia.polyglot;

import com.neo4j.magnolia.config.MagnoliaConfiguration;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Test {
    public static class MyClass {
        public int               id    = 42;
        public String            text  = "42";
        public int[]             arr   = new int[]{1, 42, 3};
        public Callable<Integer> gimme = () -> 42;
    }

    public static void main(String [] args) throws Exception {
        MagnoliaConfiguration.initialize("external/magnolia.yaml");
        MagnoliaConfiguration.getConfig().setBasePath("external/");

        ExternalFn efJS = new ExternalFn("first", "first.js", "js");
        ExternalFn echo = new ExternalFn("echo", "echo.js", "js");
        ExternalFn efPY = new ExternalFn("second", "second.py", "python");

        Value v = efJS.invoke(new MyClass(), null, null);
        System.out.println(v.getMemberKeys());
        System.out.println(v.getClass());
        Object o = ValueAdapter.convert(v);
        System.out.println("ADAPTED: " + o + " " + o.getClass());

        Value v2 = echo.invoke(3, null, null);
        Object o2 = ValueAdapter.convert(v2);
        System.out.println("ADAPTED: " + o2 + " " + o2.getClass());

        //        efPY.invoke(new MyClass(), null, null);
    }
}
