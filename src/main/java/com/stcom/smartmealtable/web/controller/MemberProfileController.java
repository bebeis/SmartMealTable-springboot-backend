package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.service.MemberProfileService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/profiles")
public class MemberProfileController {

    private final MemberProfileService memberProfileService;

    @GetMapping("/me")
    public ApiResponse<MemberProfilePageResponse> getMemberProfilePageInfo(@UserContext MemberDto memberDto) {
        MemberProfile profile = memberProfileService.getProfileFetch(memberDto.getProfileId());
        return ApiResponse.createSuccess(new MemberProfilePageResponse(profile, memberDto));
    }

    @PostMapping
    public ApiResponse<Void> createMemberProfile(@UserContext MemberDto memberDto,
                                                 @Validated @RequestBody MemberProfileRequest request) {
        memberProfileService.createProfile(request.getNickName(), memberDto.getMemberId(), request.getMemberType(),
                request.getGroupId());
        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/me")
    public ApiResponse<Void> changeMemberProfile(@UserContext MemberDto memberDto,
                                                 @Validated @RequestBody MemberProfileRequest request) {
        memberProfileService.changeProfile(memberDto.getProfileId(), request.getNickName(), request.getMemberType(),
                request.getGroupId());
        return ApiResponse.createSuccessWithNoContent();
    }

    @AllArgsConstructor
    @Data
    static class MemberProfilePageResponse {
        private String nickName;
        private String email;
        private MemberType memberType;
        private String groupName;
        private String primaryAddress;

        public MemberProfilePageResponse(MemberProfile profile, MemberDto memberDto) {
            this.nickName = profile.getNickName();
            this.email = memberDto.getEmail();
            this.primaryAddress = profile.findPrimaryAddress().getAddress().getRoadAddress()
                    + profile.findPrimaryAddress().getAddress().getDetailAddress();
            this.memberType = profile.getType();
            this.groupName = profile.getGroup().getName();
        }
    }

    @AllArgsConstructor
    @Data
    static class MemberProfileRequest {
        private String nickName;
        private Long groupId;
        private MemberType memberType;
    }
}
