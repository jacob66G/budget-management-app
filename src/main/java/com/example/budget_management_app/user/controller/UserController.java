package com.example.budget_management_app.user.controller;

import com.example.budget_management_app.user.dto.TfaQRCode;
import com.example.budget_management_app.user.dto.TfaVerifyRequest;
import com.example.budget_management_app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/2fa/setup/{id}")
    public ResponseEntity<TfaQRCode> tfaSetup(@PathVariable Long id) {
        return ResponseEntity.ok(userService.tfaSetup(id));
    }

    @PostMapping("/2fa/verify/{id}")
    public ResponseEntity<Void> verifyTfaSetup(@PathVariable Long id, @Valid @RequestBody TfaVerifyRequest request) {
        userService.verifyTfaSetup(id, request.code());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/2fa/disable/{id}")
    public ResponseEntity<Void> disableTfa(@PathVariable Long id, @Valid @RequestBody TfaVerifyRequest request) {
        userService.tfaDisable(id, request.code());
        return ResponseEntity.ok().build();
    }
}
