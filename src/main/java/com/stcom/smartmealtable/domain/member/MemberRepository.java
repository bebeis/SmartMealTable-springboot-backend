package com.stcom.smartmealtable.domain.member;

import jakarta.validation.constraints.Email;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findMemberByEmail(@Email String email);
}
