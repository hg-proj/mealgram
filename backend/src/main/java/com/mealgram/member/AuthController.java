package com.mealgram.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mealgram.member.dto.ForgotPasswordRequest;
import com.mealgram.member.dto.LoginRequest;
import com.mealgram.member.dto.LoginResponse;
import com.mealgram.member.dto.LogoutRequest;
import com.mealgram.member.dto.RefreshRequest;
import com.mealgram.member.dto.RefreshResponse;
import com.mealgram.member.dto.ResetPasswordRequest;
import com.mealgram.member.dto.SignupCodeRequest;
import com.mealgram.member.dto.SignupCodeVerifyRequest;
import com.mealgram.member.dto.SignupRequest;
import com.mealgram.member.dto.SignupResponse;
import com.mealgram.member.dto.VerifyResetCodeRequest;
import com.mealgram.member.dto.VerifyResetCodeResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// 인증 API 엔드포인트

@Tag(name = "인증")
@RestController 
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {

        SignupResponse response = authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(summary = "로그인")
    @PostMapping ("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);

    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {

        return ResponseEntity.ok(authService.refresh(request));

    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {

        authService.logout(request);

        return ResponseEntity.noContent().build();

    }

    @Operation(summary = "가입 인증번호 발송")
    @PostMapping("/signup/send-code")
    public ResponseEntity<Void> sendSignupCode(@Valid @RequestBody SignupCodeRequest request) {

        authService.sendSignupCode(request);

        return ResponseEntity.ok().build();

    }

    @Operation(summary = "가입 인증번호 확인")
    @PostMapping("/signup/verify-code")
    public ResponseEntity<Void> verifySignupCode(@Valid @RequestBody SignupCodeVerifyRequest request) {

        authService.verifySignupCode(request);

        return ResponseEntity.ok().build();

    }

    @Operation(summary = "비밀번호 재설정 인증번호 발송")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return ResponseEntity.ok().build();

    }

    @Operation(summary = "비밀번호 재설정 인증번호 확인")
    @PostMapping("/verify-reset-code")
    public ResponseEntity<VerifyResetCodeResponse> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {

        return ResponseEntity.ok(authService.verifyResetCode(request));

    }

    @Operation(summary = "비밀번호 재설정")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok().build();

    }

}
