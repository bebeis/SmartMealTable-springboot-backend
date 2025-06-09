package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.service.GroupService;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    @GetMapping()
    public ApiResponse<List<GroupDto>> searchGroup(@RequestParam String keyword) {
        if (keyword.isBlank()) {
            return ApiResponse.createError("키워드가 비어있습니다. 키워드를 입력해주세요");
        }
        List<Group> result = groupService.findGroupsByKeyword(keyword);
        return ApiResponse.createSuccess(result.stream()
                .map(GroupDto::new)
                .toList());
    }

    @Data
    @AllArgsConstructor
    static class GroupDto {
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
}
