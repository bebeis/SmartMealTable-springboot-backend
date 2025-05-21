package com.stcom.smartmealtable.service.dto;

import com.stcom.smartmealtable.domain.group.GroupType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupDto {

    private Long id;
    private GroupType groupType;
    private String address;
}
