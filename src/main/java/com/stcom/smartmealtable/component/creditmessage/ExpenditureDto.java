package com.stcom.smartmealtable.component.creditmessage;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class ExpenditureDto {

    private String vendor;
    private LocalDateTime dateTime;
    private Long amount;
    private String tradeName;

}
