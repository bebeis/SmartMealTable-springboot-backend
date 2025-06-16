package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import com.stcom.smartmealtable.service.GroupService;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import com.stcom.smartmealtable.web.dto.group.SchoolGroupCreateRequest;
import com.stcom.smartmealtable.web.dto.group.SchoolGroupUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 학교 그룹 전용 API.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schools")
public class SchoolGroupController {

    private final GroupService groupService;

    @PostMapping()
    public ApiResponse<Void> registerSchoolGroup(@RequestBody @Valid SchoolGroupCreateRequest request) {
        groupService.createSchoolGroup(new AddressRequest(request.getRoadAddress(), request.getDetailAddress()),
                request.getName(), request.getType());
        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/{id}")
    public ApiResponse<Void> editSchoolGroup(@PathVariable("id") Long id,
                                             @RequestBody @Valid SchoolGroupUpdateRequest request) {
        groupService.changeSchoolGroup(id,
                new AddressRequest(request.getRoadAddress(), request.getDetailAddress()),
                request.getName(), request.getType());
        return ApiResponse.createSuccessWithNoContent();
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSchoolGroup(@PathVariable("id") Long id) {
        groupService.deleteGroup(id);
        return ApiResponse.createSuccessWithNoContent();
    }
} 