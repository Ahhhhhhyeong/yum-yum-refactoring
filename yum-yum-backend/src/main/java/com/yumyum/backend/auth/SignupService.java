package com.yumyum.backend.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.yumyum.backend.user.ProfileRegistrationService;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 회원가입 처리
 */
@Service
public class SignupService {
    private static final Logger log = LoggerFactory.getLogger(SignupService.class);
    private final FirebaseAuth firebaseAuth;
    private final ProfileRegistrationService registration;

    public SignupService(FirebaseAuth firebaseAuth, ProfileRegistrationService registration) {
        this.firebaseAuth = firebaseAuth;
        this.registration = registration;
    }

    public void signup(SignupRequest request) throws FirebaseAuthException {
        int maxYear = LocalDate.now(ZoneId.of("Asia/Seoul")).getYear() - 15;
        if (request.birthYear() > maxYear) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "출생연도를 확인해주세요.");
        }
        // firebase authentication 생성
		UserRecord user = firebaseAuth.createUser(new UserRecord.CreateRequest()
                .setEmail(request.email())
				.setPassword(request.pw())
				.setDisplayName(request.name()));
		// Uid 호출
        String uid = user.getUid();
		// 유저 데이터 정보 저장
        try {
            registration.save(uid, request);
        } catch (RuntimeException databaseError) {
            try {
                firebaseAuth.deleteUser(uid);
            } catch (FirebaseAuthException cleanupError) {
                databaseError.addSuppressed(cleanupError);
                log.error("회원가입 복구 필요: firebaseUid={}, code={}", uid, cleanupError.getAuthErrorCode());
            }
            log.error("회원가입 DB 저장 실패: type={}", databaseError.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "프로필 저장에 실패했습니다. 잠시 후 다시 시도해주세요.", databaseError);
        }
    }
}
