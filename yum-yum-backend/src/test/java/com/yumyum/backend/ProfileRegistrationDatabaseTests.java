package com.yumyum.backend;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.yumyum.backend.auth.SignupRequest;
import com.yumyum.backend.user.ProfileRegistrationService;
import com.yumyum.backend.user.UserProfileRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

// 실제 개발 DB 사용. 정상 저장 테스트도 종료 시 롤백하여 프로필을 남기지 않습니다.
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_DB_TESTS", matches = "true")
class ProfileRegistrationDatabaseTests {
    @MockitoBean FirebaseApp firebaseApp;
    @MockitoBean FirebaseAuth firebaseAuth;
    @Autowired ProfileRegistrationService registration;
    @Autowired UserProfileRepository profiles;
    @Autowired EntityManager entityManager;

    private SignupRequest request(int weight) {
        return new SignupRequest("테스트", "db-test@example.com", "Test1234!", "male", 1998,
                175, weight, "loss", "", "light", true, true, true);
    }

    @Test
    @Transactional
    void storesAllSignupDataWithFirebaseUid() {
        String uid = "db-test-" + UUID.randomUUID();
        registration.save(uid, request(70));
        entityManager.clear();
        var profile = profiles.findByFirebaseUid(uid).orElseThrow();
        assertThat(profile.getName()).isEqualTo("테스트");
        assertThat(profile.getBirthYear()).isEqualTo((short) 1998);
        Object[] settings = (Object[]) entityManager.createNativeQuery(
                "SELECT goal_type, target_weight_kg, activity_level, water_target_ml FROM user_settings WHERE user_id = :id")
                .setParameter("id", profile.getId()).getSingleResult();
        assertThat(settings[0]).isEqualTo("loss");
        assertThat((BigDecimal) settings[1]).isEqualByComparingTo("70");
        assertThat(settings[2]).isEqualTo("light");
        assertThat(((Number) settings[3]).intValue()).isEqualTo(2600);
        Number consents = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM user_consents WHERE user_id = :id AND agreed = true")
                .setParameter("id", profile.getId()).getSingleResult();
        assertThat(consents.intValue()).isEqualTo(3);
        BigDecimal weight = (BigDecimal) entityManager.createNativeQuery(
                "SELECT weight_kg FROM weight_logs WHERE user_id = :id")
                .setParameter("id", profile.getId()).getSingleResult();
        assertThat(weight).isEqualByComparingTo("70");
    }

    @Test
    void lateDatabaseFailureRollsBackProfileSettingsAndConsents() {
        String uid = "db-test-" + UUID.randomUUID();
        // 목표는 유효하게 하여 마지막 weight_logs INSERT에서 제약조건 오류를 유발합니다.
        SignupRequest request = new SignupRequest("테스트", "db-test@example.com", "Test1234!",
                "male", 1998, 175, 301, "loss", "65", "light", true, true, true);
        assertThatThrownBy(() -> registration.save(uid, request)).isInstanceOf(RuntimeException.class);
        assertThat(profiles.findByFirebaseUid(uid)).isEmpty();
    }

    @Test
    @Transactional
    void storesAgeBasedWaterTargetsAndOptionalTargetWeights() {
        int currentYear = LocalDate.now(ZoneId.of("Asia/Seoul")).getYear();
        Object[][] cases = {
                {15, "male", null, "70", 2700},
                {18, "female", "", "70", 2000},
                {19, "male", "65.5", "65.5", 2600},
                {19, "female", null, "70", 2100}
        };
        for (Object[] example : cases) {
            String uid = "db-test-" + UUID.randomUUID();
            SignupRequest request = new SignupRequest("테스트", "db-test@example.com", "Test1234!",
                    (String) example[1], currentYear - (int) example[0], 175, 70,
                    "loss", (String) example[2], "light", true, true, true);
            registration.save(uid, request);
            Long userId = profiles.findByFirebaseUid(uid).orElseThrow().getId();
            Object[] settings = (Object[]) entityManager.createNativeQuery("""
                    SELECT target_weight_kg, water_target_ml, water_once_ml
                    FROM user_settings WHERE user_id = :id
                    """).setParameter("id", userId).getSingleResult();
            assertThat((BigDecimal) settings[0]).isEqualByComparingTo((String) example[3]);
            assertThat(((Number) settings[1]).intValue()).isEqualTo((int) example[4]);
            assertThat(((Number) settings[2]).intValue()).isEqualTo(500);
        }
    }
}
