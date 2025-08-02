package com.erp.system.purchase.enums;

public enum Priority {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    URGENT(4, "Urgent"),
    CRITICAL(5, "Critical");
    
    private final int level;
    private final String description;
    
    Priority(int level, String description) {
        this.level = level;
        this.description = description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }
}