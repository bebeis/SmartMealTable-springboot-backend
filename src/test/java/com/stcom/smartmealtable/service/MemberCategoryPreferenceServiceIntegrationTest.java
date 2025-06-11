package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.food.FoodCategory;
import com.stcom.smartmealtable.domain.food.MemberCategoryPreference;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.repository.FoodCategoryRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import java.util.List;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberCategoryPreferenceServiceIntegrationTest {

    @Autowired
    private MemberCategoryPreferenceService preferenceService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository profileRepository;

    @Autowired
    private FoodCategoryRepository categoryRepository;

    @Test
    @DisplayName("선호/비선호 음식 카테고리를 저장하고 조회할 수 있다")
    @Rollback
    void saveAndGetPreferences() throws Exception {
        // given 회원 & 프로필 & 카테고리
        Member member = Member.builder()
                .fullName("음식왕")
                .email("food@test.com")
                .rawPassword("Password1!")
                .build();
        memberRepository.save(member);

        MemberProfile profile = MemberProfile.builder()
                .member(member)
                .nickName("먹짱")
                .type(MemberType.STUDENT)
                .group(null)
                .build();
        profileRepository.save(profile);

        FoodCategory catLike = new FoodCategory();
        ReflectionTestUtils.setField(catLike, "name", "한식");
        categoryRepository.save(catLike);

        FoodCategory catDislike = new FoodCategory();
        ReflectionTestUtils.setField(catDislike, "name", "멕시칸");
        categoryRepository.save(catDislike);

        // when
        preferenceService.savePreferences(profile.getId(), List.of(catLike.getId()), List.of(catDislike.getId()));
        List<MemberCategoryPreference> prefs = preferenceService.getPreferences(profile.getId());

        // then
        assertThat(prefs).hasSize(2);
        assertThat(prefs.get(0).getType().name()).isEqualTo("LIKE");
        assertThat(prefs.get(1).getType().name()).isEqualTo("DISLIKE");
    }
} 