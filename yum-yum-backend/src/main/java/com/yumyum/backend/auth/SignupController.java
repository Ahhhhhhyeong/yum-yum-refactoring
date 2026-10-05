package com.yumyum.backend.auth;

import jakarta.validation.Valid;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.AuthErrorCode;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class SignupController {
    private static final Logger log = LoggerFactory.getLogger(SignupController.class);
    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) throws FirebaseAuthException {
        signupService.signup(request);
        return new SignupResponse(true, "회원가입이 완료되었습니다. 로그인해주세요.");
    }

    public record SignupResponse(boolean success, String message) {}

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<SignupResponse> handleSignupError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(new SignupResponse(false, exception.getReason()));
    }

    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<SignupResponse> handleFirebaseError(FirebaseAuthException exception) {
        if (exception.getAuthErrorCode() == AuthErrorCode.EMAIL_ALREADY_EXISTS) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new SignupResponse(false, "이미 가입된 이메일입니다."));
        }
        log.error("Firebase 회원가입 실패: code={}", exception.getAuthErrorCode());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new SignupResponse(false, "회원가입에 실패했습니다. 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SignupResponse> handleValidationError(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(new SignupResponse(false, "회원가입 입력값을 확인해주세요."));
    }
}
