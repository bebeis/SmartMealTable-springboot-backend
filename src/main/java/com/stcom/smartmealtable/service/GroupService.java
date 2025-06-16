package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface GroupService {
    Group findGroupByGroupId(Long groupId);

    List<Group> findGroupsByKeyword(String keyword);

    void createSchoolGroup(@NotEmpty AddressRequest addressRequest, @NotEmpty String name, @NotEmpty SchoolType type);

    void changeSchoolGroup(@NotNull Long id, AddressRequest addressRequest, @NotEmpty String name,
                           @NotNull SchoolType type);

    void deleteGroup(Long id);
}
