package com.example.budget_management_app.user.controller;

import com.example.budget_management_app.common.dto.ResponseMessage;
import com.example.budget_management_app.security.service.CustomUserDetails;
import com.example.budget_management_app.user.dto.*;
import com.example.budget_management_app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getUser(userDetails.getId()));
    }

    @GetMapping("/me/sessions")
    public ResponseEntity<List<UserSessionResponse>> getUserSessions(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentSessionId = userDetails.getSessionId();
        return ResponseEntity.ok(userService.getUserSessions(userDetails.getId(), currentSessionId));
    }

    @DeleteMapping("/me/sessions/{sessionId}")
    public ResponseEntity<Void> logoutSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.logoutSession(userDetails.getId(), sessionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateUser(
            @Valid @RequestBody UpdateUserRequest requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponse response = userService.updateUser(userDetails.getId(), requestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest requestDto
    ) {
        userService.changePassword(userDetails.getId(), requestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/close-account")
    public ResponseEntity<ResponseMessage> closeUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        ResponseMessage response = userService.closeUser(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/2fa/setup")
    public ResponseEntity<TfaQRCode> tfaSetup(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.tfaSetup(userDetails.getId()));
    }

    @PostMapping("/me/2fa/verify")
    public ResponseEntity<Void> verifyTfaSetup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TfaVerifyRequest request
    ) {
        userService.verifyTfaSetup(userDetails.getId(), request.code());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/2fa/disable")
    public ResponseEntity<Void> disableTfa(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TfaVerifyRequest request
    ) {
        userService.tfaDisable(userDetails.getId(), request.code());
        return ResponseEntity.ok().build();
    }
}
