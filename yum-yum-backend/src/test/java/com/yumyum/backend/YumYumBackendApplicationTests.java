package com.yumyum.backend;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class YumYumBackendApplicationTests {
    @MockitoBean
    private FirebaseApp firebaseApp;

    @MockitoBean
    private FirebaseAuth firebaseAuth;

    @Autowired
    private MockMvc mvc;

	@Test
	void contextLoads() {
	}

    @Test
    void signupDoesNotRequireAuthentication() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"테스트","email":"test@example.com","pw":"test-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verifyNoInteractions(firebaseAuth);
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
