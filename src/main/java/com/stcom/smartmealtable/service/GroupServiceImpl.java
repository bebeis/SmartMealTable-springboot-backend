package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.SchoolGroup;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.infrastructure.KakaoAddressApiService;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import com.stcom.smartmealtable.repository.GroupRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final KakaoAddressApiService addressApiService;

    @Override
    public Group findGroupByGroupId(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));
    }

    @Override
    public List<Group> findGroupsByKeyword(String keyword) {
        return groupRepository.findByNameContaining(keyword, Limit.of(10));
    }

    @Override
    @Transactional
    public void createSchoolGroup(AddressRequest request, String name, SchoolType type) {
        Address address = addressApiService.createAddressFromRequest(request);
        Group group = new SchoolGroup(address, name, type);
        groupRepository.save(group);
    }

    @Override
    @Transactional
    public void changeSchoolGroup(Long id, AddressRequest request, String name, SchoolType type) {
        Group foundGroup = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));
        if (!(foundGroup instanceof SchoolGroup group)) {
            throw new IllegalArgumentException("학교 그룹이 아닙니다");
        }
        Address address = addressApiService.createAddressFromRequest(request);
        group.changeNameAndAddress(name, address);
        group.changeType(type);
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다"));
        groupRepository.delete(group);
    }
}