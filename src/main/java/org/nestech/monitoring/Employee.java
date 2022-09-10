package org.nestech.monitoring;

public class Employee {

    private String name;
    private int id;
    private String code;

    public Employee(int id, String name){
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString(){
        return this.name;
    }

}
