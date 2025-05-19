package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.member.Member;
import jakarta.validation.constraints.Email;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findMemberByEmail(@Email String email);
}
