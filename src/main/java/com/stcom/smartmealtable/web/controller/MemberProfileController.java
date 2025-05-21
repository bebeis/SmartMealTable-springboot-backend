package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.group.GroupType;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.service.MemberProfileService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/profile")
public class MemberProfileController {

    private final MemberProfileService memberProfileService;

    @GetMapping("/me")
    public ApiResponse<?> getMemberProfilePageInfo(@UserContext MemberDto memberDto) {
        MemberProfile profile = memberProfileService.getProfileFetch(memberDto.getProfileId());
        return ApiResponse.createSuccess(new MemberProfilePageResponse(profile, memberDto));
    }

    @PostMapping()
    public ApiResponse<?> createMemberProfile(@UserContext MemberDto memberDto,
                                              @Validated @RequestBody MemberProfileCreateRequest request) {
        memberProfileService.createProfile(request.getNickName(), memberDto.getMemberId(), request.getGroupType(),
                request.getGroupId());
        return ApiResponse.createSuccessWithNoContent();
    }

    @AllArgsConstructor
    @Data
    static class MemberProfilePageResponse {
        private String nickName;
        private String email;
        private GroupType groupType;
        private String groupName;
        private String primaryAddress;

        public MemberProfilePageResponse(MemberProfile profile, MemberDto memberDto) {
            this.nickName = profile.getNickName();
            this.email = memberDto.getEmail();
            Address address = profile.findPrimaryAddress().getAddress();
            this.primaryAddress = address.getRoadAddress() + address.getDetailAddress();
            this.groupType = profile.getGroup().getGroupType();
            this.groupName = profile.getGroup().getName();
        }
    }

    @AllArgsConstructor
    @Data
    static class MemberProfileCreateRequest {
        private String nickName;
        private Long groupId;
        private GroupType groupType;
    }

}
