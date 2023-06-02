package org.nestech.monitoring.model;

public class DayOff {
    public int id;
    public String name;
    public int is_paid;
    public int total_days;
    public int total_hours;
    public AbsenceJustifications absence_justifications;
    public DayOffRequests day_off_requests;
}
