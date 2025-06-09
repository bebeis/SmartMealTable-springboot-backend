package com.stcom.smartmealtable.domain.group;

import com.stcom.smartmealtable.domain.Address.Address;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "affiliation")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@Getter
@NoArgsConstructor
public abstract class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "affiliation_id")
    private Long id;

    @Embedded
    private Address address;

    private String name;

    public Group(Address address, String name) {
        this.address = address;
        this.name = name;
    }

    public abstract String getTypeName();

    public void changeNameAndAddress(String name, Address address) {
        this.name = name;
        this.address = address;
    }

}
