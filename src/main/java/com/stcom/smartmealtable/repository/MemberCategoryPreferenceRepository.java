package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.food.MemberCategoryPreference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberCategoryPreferenceRepository extends JpaRepository<MemberCategoryPreference, Long> {

    @Query("select mcp from MemberCategoryPreference mcp where mcp.memberProfile.id = :memberProfileId order by mcp.priority asc")
    List<MemberCategoryPreference> findDefaultByMemberProfileId(Long memberProfileId);
    
    void deleteByMemberProfile_Id(Long memberProfileId);
} 