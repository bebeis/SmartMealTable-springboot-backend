package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("회원을 저장하고 ID로 조회할 수 있어야 한다")
    void saveAndFindById() throws PasswordPolicyException {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .fullName("홍길동")
                .rawPassword("Password123!")
                .build();

        // when
        Member savedMember = memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Member> foundMember = memberRepository.findById(savedMember.getId());
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundMember.get().getFullName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("이메일로 회원을 조회할 수 있어야 한다")
    void findByEmail() throws PasswordPolicyException {
        // given
        String email = "test@example.com";
        Member member = Member.builder()
                .email(email)
                .fullName("홍길동")
                .rawPassword("Password123!")
                .build();

        memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Member> foundMember = memberRepository.findByEmail(email);

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환해야 한다")
    void findByEmailNotFound() {
        // given
        String nonExistentEmail = "nonexistent@example.com";

        // when
        Optional<Member> foundMember = memberRepository.findByEmail(nonExistentEmail);

        // then
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("회원을 삭제할 수 있어야 한다")
    void deleteMember() throws PasswordPolicyException {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .fullName("홍길동")
                .rawPassword("Password123!")
                .build();

        Member savedMember = memberRepository.save(member);
        entityManager.flush();

        // when
        memberRepository.delete(savedMember);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Member> foundMember = memberRepository.findById(savedMember.getId());
        assertThat(foundMember).isEmpty();
    }
} 