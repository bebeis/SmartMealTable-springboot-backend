package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.group.Group;
import com.stcom.smartmealtable.domain.group.SchoolType;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import com.stcom.smartmealtable.service.GroupService;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/schools")
    public ApiResponse<?> registerSchoolGroup(@RequestBody @Validated SchoolGroupCreateRequest request) {
        groupService.createSchoolGroup(new AddressRequest(request.getRoadAddress(), request.getDetailAddress()),
                request.getName(), request.getType());
        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/schools/{id}")
    public ApiResponse<?> editSchoolGroup(@PathVariable("id") Long id,
                                          @RequestBody @Validated SchoolGroupUpdateRequest request) {
        groupService.changeSchoolGroup(id,
                new AddressRequest(request.getRoadAddress(), request.getDetailAddress()),
                request.getName(), request.getType());
        return ApiResponse.createSuccessWithNoContent();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteGroup(@PathVariable("id") Long id) {
        groupService.deleteGroup(id);
        return ApiResponse.createSuccessWithNoContent();
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

    @Data
    @AllArgsConstructor
    static class SchoolGroupCreateRequest {

        @NotEmpty
        private String roadAddress;

        @NotEmpty
        private String detailAddress;

        @NotEmpty
        private String name;

        @NotNull
        private SchoolType type;
    }

    @Data
    @AllArgsConstructor
    static class SchoolGroupUpdateRequest {

        @NotEmpty
        private String roadAddress;

        @NotEmpty
        private String detailAddress;

        @NotEmpty
        private String name;

        @NotNull
        private SchoolType type;
    }
}
