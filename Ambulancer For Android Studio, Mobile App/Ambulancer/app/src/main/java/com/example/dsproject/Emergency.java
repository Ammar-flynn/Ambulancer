package com.example.dsproject;

import java.io.Serializable;

public class Emergency implements Serializable {
    public String id;
    public String name;
    public String address;
    public String symptoms;
    public String type;
    public int subPriority;
    public String status;
    public long timestamp;
    public String track;
    public String HospitalAssigned;

    public Emergency(String id, String name, String address, String symptoms,
                     String type, int subPriority, String status,
                     String track, String HospitalAssigned) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.symptoms = symptoms;
        this.type = type;
        this.subPriority = subPriority;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
        this.track = track;
        this.HospitalAssigned = HospitalAssigned;
    }
}