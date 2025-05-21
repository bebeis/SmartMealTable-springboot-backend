package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.Address.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    
}
