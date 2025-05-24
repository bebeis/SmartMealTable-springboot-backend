package com.stcom.smartmealtable.domain.group;

public enum SchoolType {
    UNIVERSITY_FOUR_YEAR("대학교(4년제)"), UNIVERSITY_TWO_YEAR("대학교(2년제)"),
    HIGH_SCHOOL("고등학교"), MIDDLE_SCHOOL("중학교");

    private final String description;

    SchoolType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
