package com.stcom.smartmealtable.web.dto.group;

import com.stcom.smartmealtable.domain.group.SchoolType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SchoolGroupCreateRequest {

    @NotEmpty
    private String roadAddress;

    @NotEmpty
    private String detailAddress;

    @NotEmpty
    private String name;

    @NotNull
    private SchoolType type;
} 