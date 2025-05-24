package com.stcom.smartmealtable.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TermAgreementRequestDto {
    private Long termId;
    private Boolean isAgreed;
}
