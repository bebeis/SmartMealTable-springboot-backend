package com.stcom.smartmealtable.repository;

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
}
