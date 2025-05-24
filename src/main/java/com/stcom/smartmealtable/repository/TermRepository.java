package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.term.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {
}
