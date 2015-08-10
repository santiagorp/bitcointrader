package com.srp.finance;

public enum Trend {
    Undefined("Undefined"),
    Up("Up"),
    Down("Down");

    private String name;

    private Trend(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
