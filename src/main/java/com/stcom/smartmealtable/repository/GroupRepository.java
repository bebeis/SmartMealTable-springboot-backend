package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.group.Group;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByNameContaining(String name, Limit limit);
}
