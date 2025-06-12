package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressEntity;
import com.stcom.smartmealtable.domain.Address.AddressType;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class AddressEntityRepositoryTest {

    @Autowired
    private AddressEntityRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("주소 엔티티를 저장하고 조회할 수 있다")
    void saveAndFind() {
        // given
        Address address = Address.builder()
                .roadAddress("서울특별시 중구 세종대로 110")
                .detailAddress("별관")
                .build();
        AddressEntity entity = AddressEntity.builder()
                .address(address)
                .type(AddressType.HOME)
                .alias("우리집")
                .build();
        repository.save(entity);
        em.flush();
        em.clear();

        // when
        Optional<AddressEntity> found = repository.findById(entity.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getAlias()).isEqualTo("우리집");
    }
} 