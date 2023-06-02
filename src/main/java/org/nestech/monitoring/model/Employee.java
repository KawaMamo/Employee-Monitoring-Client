package org.nestech.monitoring.model;

import java.util.Date;

public class Employee {
    public int id;
    public String name;
    public int municipality_id;
    public int department_id;
    public int candidate_id;
    public String national_id;
    public Date created_at;
    public Date updated_at;
    public Pivot pivot;
}
