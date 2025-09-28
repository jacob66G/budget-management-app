package com.example.budget_management_app.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LoginResponseDto {
        private String accessToken;
        private Long userId;
        private String name;
        private String surname;
        private String email;
        private Boolean isTfaRequired;

    public LoginResponseDto(Long userId, Boolean isTfaRequired) {
        this.userId = userId;
        this.isTfaRequired = isTfaRequired;
    }
}
