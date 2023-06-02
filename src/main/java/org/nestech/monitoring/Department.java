package org.nestech.monitoring;

public class Department {
    public String name;

    @Override
    public String toString() {
        if(Translator.map.containsKey(name)){
            return Translator.map.get(name);
        }
        return name;
    }

    public Department(String name) {
        this.name = name;
    }
}
