package com.stcom.smartmealtable.domain.group;

import com.stcom.smartmealtable.domain.Address.Address;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class SchoolGroup extends Group {

    public SchoolGroup(Address address, String name, SchoolType schoolType) {
        super(address, name);
        this.schoolType = schoolType;
    }

    @Enumerated(EnumType.STRING)
    private SchoolType schoolType;

    @Override
    public String getTypeName() {
        return schoolType.name();
    }

    public void changeType(SchoolType schoolType) {
        this.schoolType = schoolType;
    }
}
