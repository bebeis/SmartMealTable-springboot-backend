package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {

    @Query("select mp from MemberProfile mp where mp.member.id = :memberId")
    Optional<MemberProfile> findMemberProfileByMemberId(@Param("memberId") Long memberId);

    void deleteMemberProfileByMember(Member member);

    @EntityGraph(attributePaths = {"member", "addressHistory", "group"})
    Optional<MemberProfile> findMemberProfileEntityGraphById(Long id);
}
