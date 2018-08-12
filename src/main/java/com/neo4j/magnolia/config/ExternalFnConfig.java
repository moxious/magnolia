package com.neo4j.magnolia.config;

public class ExternalFnConfig {
    public String name;
    public String file;
    public String language = "js";

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
        return "ExternalFnConfig { name: " + getName() + ", file: " + getFile() + ", language: " + getLanguage() + " }\n";
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int hashCode() {
        return file.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        ExternalFnConfig other = (ExternalFnConfig) o;

        return getName().equals(other.getName()) &&
                getLanguage().equals(other.getLanguage()) &&
                getFile().equals(other.getFile());
    }
}
