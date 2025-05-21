package com.stcom.smartmealtable.domain.group;

public enum IndustryType {
    IT("IT"), FINANCE("파이낸스"), MANUFACTURING("제조업"), SERVICE("서비스");

    private final String description;

    IndustryType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
