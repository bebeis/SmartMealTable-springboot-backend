package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.term.Term;
import com.stcom.smartmealtable.service.TermService;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/terms")
public class TermController {

    private final TermService termService;

    @GetMapping()
    public ApiResponse<?> getTerms() {
        List<Term> result = termService.findAll();
        return ApiResponse.createSuccess(result.stream()
                .map(TermResponse::new)
                .toList());
    }

    @Data
    @AllArgsConstructor
    static class TermResponse {
        private Long termId;
        private String title;
        private String content;
        private boolean isRequired;

        public TermResponse(Term term) {
            this.termId = term.getId();
            this.title = term.getTitle();
            this.content = term.getContent();
            this.isRequired = term.getIsRequired();
        }

    }

}
