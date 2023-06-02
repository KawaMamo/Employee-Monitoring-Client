package org.nestech.monitoring.model;

import java.util.ArrayList;

public class EmployeeData {

    public int id;
    public String name;
    public String national_id;
    public ActiveSystem active_system;
    public ArrayList<Object> work_system_archive;
    public ArrayList<DayOff> day_offs;
    public int is_paid;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNational_id() {
        return national_id;
    }

    public ActiveSystem getActive_system() {
        return active_system;
    }

    public ArrayList<Object> getWork_system_archive() {
        return work_system_archive;
    }

    public ArrayList<DayOff> getDay_offs() {
        return day_offs;
    }

    public int getIs_paid() {
        return is_paid;
    }
}
