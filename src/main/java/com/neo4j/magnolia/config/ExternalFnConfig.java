package com.neo4j.magnolia.config;

public class ExternalFnConfig {
    private String name;
    private String file;
    private String language = "js";

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "name: " + getName() + "\n" + "file: " + getFile() + "\n" + "language: " + getLanguage() + "\n";
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
