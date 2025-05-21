package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {

}
