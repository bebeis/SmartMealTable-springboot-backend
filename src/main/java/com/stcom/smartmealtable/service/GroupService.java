package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public interface GroupService {
    Group findGroupByGroupId(Long groupId);

    List<Group> findGroupsByKeyword(String keyword);

    void createSchoolGroup(@NotEmpty AddressRequest addressRequest, @NotEmpty String name, @NotEmpty SchoolType type);

    void changeSchoolGroup(@NotEmpty Long id, AddressRequest addressRequest, @NotEmpty String name,
                           @NotEmpty SchoolType type);

    void deleteGroup(Long id);
}
