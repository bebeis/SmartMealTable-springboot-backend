package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.term.Term;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TermRepositoryTest {

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("약관을 저장하고 조회할 수 있어야 한다")
    void saveAndFind() {
        // given
        Term term = createTerm("테스트 약관", true);
        termRepository.save(term);
        em.flush();
        em.clear();

        // when
        Optional<Term> found = termRepository.findById(term.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("테스트 약관");
        assertThat(found.get().getIsRequired()).isTrue();
    }

    private Term createTerm(String title, boolean required) {
        Term term = new Term();
        try {
            java.lang.reflect.Field titleField = Term.class.getDeclaredField("title");
            titleField.setAccessible(true);
            titleField.set(term, title);

            java.lang.reflect.Field isRequiredField = Term.class.getDeclaredField("isRequired");
            isRequiredField.setAccessible(true);
            isRequiredField.set(term, required);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return term;
    }
} 