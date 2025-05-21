package com.stcom.smartmealtable.domain.group;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class SchoolGroup extends Group {

    @Enumerated(EnumType.STRING)
    private SchoolType schoolType;
}
