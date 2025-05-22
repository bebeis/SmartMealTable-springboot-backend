package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.group.Group;
import java.util.List;

public interface GroupService {
    Group findGroupByGroupId(Long groupId);
    List<Group> findGroupsByKeyword(String keyword);
}
