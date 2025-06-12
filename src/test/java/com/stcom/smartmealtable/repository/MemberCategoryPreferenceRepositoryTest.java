package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.food.FoodCategory;
import com.stcom.smartmealtable.domain.food.MemberCategoryPreference;
import com.stcom.smartmealtable.domain.food.PreferenceType;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
class MemberCategoryPreferenceRepositoryTest {

    @Autowired
    private MemberCategoryPreferenceRepository repository;

    @Autowired
    private MemberProfileRepository profileRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FoodCategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("프로필 ID 로 기본 정렬 선호/비선호 카테고리를 조회할 수 있다")
    void findDefaultByMemberProfileId() throws Exception {
        // given
        Member member = new Member("pref@test.com");
        memberRepository.save(member);

        MemberProfile profile = MemberProfile.builder()
                .member(member)
                .nickName("닉")
                .type(MemberType.WORKER)
                .group(null)
                .build();
        profileRepository.save(profile);

        FoodCategory korean = new FoodCategory();
        ReflectionTestUtils.setField(korean, "name", "한식");
        categoryRepository.save(korean);
        FoodCategory sushi = new FoodCategory();
        ReflectionTestUtils.setField(sushi, "name", "일식");
        categoryRepository.save(sushi);

        MemberCategoryPreference like = MemberCategoryPreference.builder()
                .memberProfile(profile)
                .category(korean)
                .type(PreferenceType.LIKE)
                .priority(1)
                .build();
        MemberCategoryPreference dislike = MemberCategoryPreference.builder()
                .memberProfile(profile)
                .category(sushi)
                .type(PreferenceType.DISLIKE)
                .priority(1)
                .build();
        repository.saveAll(List.of(like, dislike));
        em.flush();
        em.clear();

        // when
        List<MemberCategoryPreference> prefs = repository.findDefaultByMemberProfileId(profile.getId());

        // then
        assertThat(prefs).hasSize(2);
        assertThat(prefs.get(0).getType()).isEqualTo(PreferenceType.LIKE);
    }
} 