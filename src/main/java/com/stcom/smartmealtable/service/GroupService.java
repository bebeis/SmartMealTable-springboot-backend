package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.repository.GroupRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;

    public Group findGroupByGroupId(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));
    }

    public List<Group> findGroupsByKeyword(String keyword) {
        return groupRepository.findByNameContaining(keyword, Limit.of(10));
    }
}
