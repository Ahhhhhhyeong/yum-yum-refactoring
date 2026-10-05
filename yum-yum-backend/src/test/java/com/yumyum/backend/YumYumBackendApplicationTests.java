package com.yumyum.backend;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration")
@AutoConfigureMockMvc
class YumYumBackendApplicationTests {
    @MockitoBean
    private FirebaseApp firebaseApp;

    @MockitoBean
    private FirebaseAuth firebaseAuth;

    @MockitoBean
    private com.yumyum.backend.user.ProfileRegistrationService registration;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void configureSecurityTestContext() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

	@Test
	void contextLoads() {
	}

    @Test
    void signupDoesNotRequireAuthentication() throws Exception {
        UserRecord user = mock(UserRecord.class);
        when(user.getUid()).thenReturn("new-uid");
        when(firebaseAuth.createUser(any(UserRecord.CreateRequest.class))).thenReturn(user);
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"테스트","email":"test@example.com","pw":"Test1234!","gender":"male","birthYear":1998,"height":175,"weight":70,"goals":"loss","targetWeight":"","targetExercise":"light","service":true,"privacy":true,"sensitive":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(firebaseAuth).createUser(any(UserRecord.CreateRequest.class));
        verify(registration).save(eq("new-uid"), any());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        FirebaseAuthException error = mock(FirebaseAuthException.class);
        when(error.getAuthErrorCode()).thenReturn(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        when(firebaseAuth.createUser(any(UserRecord.CreateRequest.class))).thenThrow(error);
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"테스트","email":"test@example.com","pw":"Test1234!","gender":"male","birthYear":1998,"height":175,"weight":70,"goals":"loss","targetWeight":"","targetExercise":"light","service":true,"privacy":true,"sensitive":true}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }

    @Test
    void weakPasswordDoesNotCreateAccount() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"테스트","email":"test@example.com","pw":"short","gender":"male","birthYear":1998,"height":175,"weight":70,"goals":"loss","targetWeight":"","targetExercise":"light","service":true,"privacy":true,"sensitive":true}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(firebaseAuth);
    }

    @Test
    void firebaseFailureDoesNotReturnSuccess() throws Exception {
        when(firebaseAuth.createUser(any(UserRecord.CreateRequest.class)))
                .thenThrow(mock(FirebaseAuthException.class));
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"테스트","email":"test@example.com","pw":"Test1234!","gender":"male","birthYear":1998,"height":175,"weight":70,"goals":"loss","targetWeight":"","targetExercise":"light","service":true,"privacy":true,"sensitive":true}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false));
        verifyNoInteractions(registration);
    }

    @Test
    void databaseFailureDeletesOnlyTheNewFirebaseAccount() throws Exception {
        UserRecord user = mock(UserRecord.class);
        when(user.getUid()).thenReturn("new-uid");
        when(firebaseAuth.createUser(any(UserRecord.CreateRequest.class))).thenReturn(user);
        doThrow(new org.springframework.dao.DataIntegrityViolationException("DB unavailable"))
                .when(registration).save(eq("new-uid"), any());
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"테스트","email":"test@example.com","pw":"Test1234!",
                                 "gender":"male","birthYear":1998,"height":175,"weight":70,
                                 "goals":"loss","targetWeight":"","targetExercise":"light",
                                 "service":true,"privacy":true,"sensitive":true}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false));
        verify(firebaseAuth).deleteUser("new-uid");
    }

    @Test
    void invalidProfileDoesNotCreateFirebaseAccount() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"테스트","email":"test@example.com","pw":"Test1234!",
                                 "gender":"male","birthYear":2999,"height":175,"weight":70,
                                 "goals":"loss","targetWeight":"","targetExercise":"light",
                                 "service":true,"privacy":true,"sensitive":true}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(firebaseAuth, registration);
    }

    @Test
    void cleanupFailureStillReturnsSignupFailure() throws Exception {
        UserRecord user = mock(UserRecord.class);
        when(user.getUid()).thenReturn("new-uid");
        when(firebaseAuth.createUser(any(UserRecord.CreateRequest.class))).thenReturn(user);
        doThrow(new org.springframework.dao.DataIntegrityViolationException("DB unavailable"))
                .when(registration).save(eq("new-uid"), any());
        doThrow(mock(FirebaseAuthException.class)).when(firebaseAuth).deleteUser("new-uid");
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"테스트","email":"test@example.com","pw":"Test1234!",
                                 "gender":"male","birthYear":1998,"height":175,"weight":70,
                                 "goals":"loss","targetWeight":"","targetExercise":"light",
                                 "service":true,"privacy":true,"sensitive":true}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void missingTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        verifyNoInteractions(firebaseAuth);
    }

    @Test
    void invalidTokenIsUnauthorized() throws Exception {
        when(firebaseAuth.verifyIdToken("invalid-token", true))
                .thenThrow(mock(FirebaseAuthException.class));
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate",
                        org.hamcrest.Matchers.containsString("invalid_token")));
    }

    @Test
    void verifiedTokenReturnsTheVerifiedUserWithoutCreatingASession() throws Exception {
        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn("firebase-uid");
        when(token.getEmail()).thenReturn("test@example.com");
        when(firebaseAuth.verifyIdToken("valid-token", true)).thenReturn(token);

        var result = mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("firebase-uid"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andReturn();
        org.assertj.core.api.Assertions.assertThat(result.getRequest().getSession(false)).isNull();
        verify(firebaseAuth).verifyIdToken("valid-token", true);
        // 이전 요청의 인증이 다음 요청으로 넘어가지 않아야 합니다.
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void authenticatedUserWithoutUserRoleIsForbidden() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isForbidden());
    }
}
