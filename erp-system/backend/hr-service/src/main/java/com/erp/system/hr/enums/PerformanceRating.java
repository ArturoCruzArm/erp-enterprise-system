package com.erp.system.hr.enums;

public enum PerformanceRating {
    OUTSTANDING(5, "Outstanding"),
    EXCEEDS_EXPECTATIONS(4, "Exceeds Expectations"),
    MEETS_EXPECTATIONS(3, "Meets Expectations"),
    BELOW_EXPECTATIONS(2, "Below Expectations"),
    UNSATISFACTORY(1, "Unsatisfactory");
    
    private final int value;
    private final String description;
    
    PerformanceRating(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
}