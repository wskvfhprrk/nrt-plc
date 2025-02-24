package com.jc.entity;

public class OutputPoint {
    private String name;
    private String pointName;
    private String status;

    public OutputPoint(String name, String pointName,  String status) {
        this.name = name;
        this.pointName = pointName;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}