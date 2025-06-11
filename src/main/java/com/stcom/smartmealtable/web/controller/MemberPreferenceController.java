package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.food.MemberCategoryPreference;
import com.stcom.smartmealtable.domain.food.PreferenceType;
import com.stcom.smartmealtable.service.MemberCategoryPreferenceService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/preferences")
public class MemberPreferenceController {

    private final MemberCategoryPreferenceService memberCategoryPreferenceService;

    @GetMapping
    public ApiResponse<PreferencesResponse> getCategoryPreferences(@UserContext MemberDto memberDto) {
        List<MemberCategoryPreference> preferences =
                memberCategoryPreferenceService.getPreferences(memberDto.getProfileId());

        List<CategoryPreferenceDto> liked = preferences.stream()
                .filter(p -> p.getType() == PreferenceType.LIKE)
                .map(p -> new CategoryPreferenceDto(
                        p.getCategory().getId(),
                        p.getCategory().getName(),
                        p.getPriority()))
                .toList();

        List<CategoryPreferenceDto> disliked = preferences.stream()
                .filter(p -> p.getType() == PreferenceType.DISLIKE)
                .map(p -> new CategoryPreferenceDto(
                        p.getCategory().getId(),
                        p.getCategory().getName(),
                        p.getPriority()))
                .toList();

        return ApiResponse.createSuccess(new PreferencesResponse(liked, disliked));
    }

    @PostMapping
    public ApiResponse<Void> saveCategoryPreferences(@UserContext MemberDto memberDto,
                                                     @RequestBody PreferencesRequest request) {
        memberCategoryPreferenceService.savePreferences(
                memberDto.getProfileId(),
                request.getLiked(),
                request.getDisliked());
        return ApiResponse.createSuccessWithNoContent();
    }

    @AllArgsConstructor
    @Data
    static class PreferencesRequest {
        private List<Long> liked;
        private List<Long> disliked;
    }

    @AllArgsConstructor
    @Data
    static class PreferencesResponse {
        private List<CategoryPreferenceDto> liked;
        private List<CategoryPreferenceDto> disliked;
    }

    @AllArgsConstructor
    @Data
    static class CategoryPreferenceDto {
        private Long categoryId;
        private String categoryName;
        private Integer priority;
    }
} 