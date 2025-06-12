package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class MemberProfileRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository profileRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("MemberProfileEntityGraph 조회시 member 가 fetch 되어야 한다")
    void findEntityGraph() {
        // given
        Member member = new Member("graph@test.com");
        memberRepository.save(member);

        MemberProfile profile = MemberProfile.builder()
                .member(member)
                .nickName("graph")
                .type(MemberType.STUDENT)
                .group(null)
                .build();
        profileRepository.save(profile);
        em.flush();
        em.clear();

        // when
        var found = profileRepository.findMemberProfileEntityGraphById(profile.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getMember().getEmail()).isEqualTo("graph@test.com");
    }
} 