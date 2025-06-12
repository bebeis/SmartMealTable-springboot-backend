package com.stcom.smartmealtable.web.dto.group;

import com.stcom.smartmealtable.domain.group.Group;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 그룹 검색 응답용 DTO.
 */
@Data
@AllArgsConstructor
public class GroupDto {
    private Long id;
    private String roadAddress;
    private String name;
    private String groupType;

    public GroupDto(Group group) {
        this.id = group.getId();
        this.groupType = group.getTypeName();
        this.name = group.getName();
        this.roadAddress = group.getAddress().getRoadAddress();
    }
} 