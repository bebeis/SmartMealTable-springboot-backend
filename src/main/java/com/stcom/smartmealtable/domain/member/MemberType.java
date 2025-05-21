package com.stcom.smartmealtable.domain.member;

import jakarta.persistence.Embeddable;

@Embeddable
public enum MemberType {
    STUDENT, WORKER, OTHER
}
