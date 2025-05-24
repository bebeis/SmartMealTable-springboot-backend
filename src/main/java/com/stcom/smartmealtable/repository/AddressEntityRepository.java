package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.Address.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressEntityRepository extends JpaRepository<AddressEntity, Long> {

}
