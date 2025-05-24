package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);

    @Query("select sa.member.id from SocialAccount sa where sa.provider = :provider and sa.providerUserId = :providerUserId")
    Optional<Long> findMemberIdByProviderAndProviderUserId(
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId
    );

    List<SocialAccount> findAllByMemberId(Long memberId);

    void deleteSocialAccountByMember(Member member);

    /**
     * 소셜 계정(provider, providerUserId)에 연결된 MemberProfile의 ID만 조회
     */
    @Query("""
      select p.id 
      from SocialAccount sa
      join sa.member m
      join m.memberProfile p
      where sa.provider = :provider
        and sa.providerUserId = :providerUserId
      """)
    Optional<Long> findProfileIdByProviderAndProviderUserId(
        @Param("provider") String provider,
        @Param("providerUserId") String providerUserId
    );
}
