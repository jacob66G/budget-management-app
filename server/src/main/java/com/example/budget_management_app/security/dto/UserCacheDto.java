package com.example.budget_management_app.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCacheDto implements Serializable {
    private Long id;
    private String email;
    private String password;
}
