package com.yumyum.backend.auth;

import jakarta.validation.Valid;
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

    // 연결 확인용: 계정 생성이나 데이터 저장은 수행하지 않습니다.
    @PostMapping("/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        log.info("회원가입 요청 수신: name={}, email={}, gender={}, age={}, height={}, weight={}, "
                        + "goals={}, targetWeight={}, targetExercise={}, service={}, privacy={}, sensitive={}, "
                        + "passwordProvided={}",
                request.name(), request.email(), request.gender(), request.age(), request.height(),
                request.weight(), request.goals(), request.targetWeight(), request.targetExercise(),
                request.service(), request.privacy(), request.sensitive(), !request.pw().isBlank());
        
        return new SignupResponse(true, "백엔드에서 회원가입 요청을 수신했습니다. (계정 생성 전)");
    }

    public record SignupResponse(boolean success, String message) {}

    // 회원가입 진행

}
