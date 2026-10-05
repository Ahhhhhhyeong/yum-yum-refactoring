package com.yumyum.backend.user;

import com.yumyum.backend.auth.SignupRequest;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileRegistrationService {
    private final UserProfileRepository profiles;
    private final EntityManager entityManager;
    private final String termsVersion;
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    public ProfileRegistrationService(UserProfileRepository profiles, EntityManager entityManager,
            @Value("signup.terms-version") String termsVersion) {
        this.profiles = profiles;
        this.entityManager = entityManager;
        this.termsVersion = termsVersion;
    }

    // 별도 Bean의 트랜잭션이 커밋된 후 SignupService로 돌아갑니다.
    @Transactional
    public void save(String firebaseUid, SignupRequest request) {
        UserProfile profile = profiles.saveAndFlush(new UserProfile(firebaseUid, request));
        Long userId = profile.getId();
        BigDecimal targetWeight = request.targetWeight() == null || request.targetWeight().isBlank()
                ? BigDecimal.valueOf(request.weight()) : new BigDecimal(request.targetWeight());
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        int age = today.getYear() - request.birthYear();
        int waterTarget = WaterIntakeCalculator.calculate(age, request.gender());

        entityManager.createNativeQuery("""
                INSERT INTO user_settings
                    (user_id, goal_type, target_weight_kg, activity_level, water_once_ml, water_target_ml)
                VALUES (:userId, :goal, :weight, :activity, 500, :water)
                """).setParameter("userId", userId).setParameter("goal", request.goals())
                .setParameter("weight", targetWeight).setParameter("activity", request.targetExercise())
                .setParameter("water", waterTarget).executeUpdate();
        saveConsent(userId, "service", request.service());
        saveConsent(userId, "privacy", request.privacy());
        saveConsent(userId, "sensitive", request.sensitive());
        entityManager.createNativeQuery("""
                INSERT INTO weight_logs (user_id, record_date, weight_kg)
                VALUES (:userId, :date, :weight)
                """).setParameter("userId", userId).setParameter("date", today)
                .setParameter("weight", BigDecimal.valueOf(request.weight())).executeUpdate();
    }

    private void saveConsent(Long userId, String type, boolean agreed) {
        entityManager.createNativeQuery("""
                INSERT INTO user_consents (user_id, consent_type, terms_version, agreed)
                VALUES (:userId, :type, :version, :agreed)
                """).setParameter("userId", userId).setParameter("type", type)
                .setParameter("version", termsVersion).setParameter("agreed", agreed).executeUpdate();
    }
}
