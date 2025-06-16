package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.component.creditmessage.CreditMessageManager;
import com.stcom.smartmealtable.component.creditmessage.ExpenditureDto;
import com.stcom.smartmealtable.domain.Budget.Expenditure;
import com.stcom.smartmealtable.service.ExpenditureService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/members/me/expenditures")
public class MemberExpenditureController {

    private final ExpenditureService expenditureService;
    private final CreditMessageManager creditMessageManager;

    @PostMapping("/messages/parse")
    public ApiResponse<ExpenditureDto> parseCreditMessage(@RequestBody ParseRequest request) {
        return ApiResponse.createSuccess(creditMessageManager.parseMessage(request.getMessage()));
    }

    @GetMapping
    public ApiResponse<Slice<ExpenditureResponse>> getExpenditures(@UserContext MemberDto memberDto,
                                                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        Slice<Expenditure> slice = expenditureService.getExpenditures(memberDto.getProfileId(), page, size);
        Slice<ExpenditureResponse> responseSlice = slice.map(ExpenditureResponse::of);
        return ApiResponse.createSuccess(responseSlice);
    }

    @PostMapping
    public ApiResponse<Void> registerExpenditure(@UserContext MemberDto memberDto,
                                                 @RequestBody @Validated ExpenditureRequest request) {
        expenditureService.registerExpenditure(
                memberDto.getProfileId(),
                request.getSpentDate(),
                request.getAmount(),
                request.getTradeName()
        );
        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/{id}")
    public ApiResponse<Void> editExpenditure(@UserContext MemberDto memberDto, @PathVariable("id") Long expenditureId,
                                             @RequestBody @Validated ExpenditureRequest request) {
        expenditureService.editExpenditure(
                memberDto.getProfileId(),
                expenditureId,
                request.getSpentDate(),
                request.getAmount(),
                request.getTradeName()
        );
        return ApiResponse.createSuccessWithNoContent();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteExpenditure(@UserContext MemberDto memberDto,
                                               @PathVariable("id") Long expenditureId) {
        expenditureService.deleteExpenditure(memberDto.getProfileId(), expenditureId);
        return ApiResponse.createSuccessWithNoContent();
    }

    @Data
    static class ParseRequest {

        @NotEmpty
        private String message;

    }

    @Data
    static class ExpenditureRequest {

        @DateTimeFormat(iso = ISO.DATE_TIME)
        @NotNull
        private LocalDateTime spentDate;

        @NotNull
        @Positive
        private Long amount;

        @NotEmpty
        private String tradeName;
    }

    @Data
    @AllArgsConstructor
    static class ExpenditureResponse {

        private Long id;
        private LocalDateTime spentDate;
        private Long amount;
        private String tradeName;

        public static ExpenditureResponse of(Expenditure expenditure) {
            return new ExpenditureResponse(expenditure.getId(), expenditure.getSpentDate(), expenditure.getAmount(),
                    expenditure.getTradeName());
        }
    }
}
