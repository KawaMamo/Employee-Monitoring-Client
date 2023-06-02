package org.nestech.monitoring.model;

import java.util.ArrayList;

public class ActiveSystem {
    public int id;
    public String name;
    public int cycle_day;
    public String starting_from;
    public String date;
    public ArrayList<SubShift> sub_shifts;
    public ArrayList<Employee> employees;
    public ArrayList<WorkingInformation> working_information;
    public Municipality municipality;
}
