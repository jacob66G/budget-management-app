package com.example.budget_management_app.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LoginResponse {
        private Long userId;
        private String name;
        private String surname;
        private String email;
        private String status;
        private Boolean mfaEnabled;
        private Instant createdAt;
        private String accessToken;
        private Boolean isMfaRequired;

    public LoginResponse(Long userId, Boolean isMfaRequired) {
        this.userId = userId;
        this.isMfaRequired = isMfaRequired;
    }
}
