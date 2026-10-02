package com.yumyum.backend.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import java.util.List;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FirebaseAuthenticationProvider implements AuthenticationProvider {
    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthenticationProvider(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String idToken = ((BearerTokenAuthenticationToken) authentication).getToken();
        try {
            // 서명·만료·발급자·대상 프로젝트와 함께 폐기/사용 중지 여부도 확인합니다.
            FirebaseToken token = firebaseAuth.verifyIdToken(idToken, true);
            FirebasePrincipal principal = new FirebasePrincipal(token.getUid(), token.getEmail());
            // 클라이언트가 보낸 역할은 신뢰하지 않고, 인증된 사용자에게 USER 권한만 부여합니다.
            return UsernamePasswordAuthenticationToken.authenticated(
                    principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        } catch (FirebaseAuthException | IllegalArgumentException exception) {
            // 토큰 원문이나 Firebase 내부 오류를 응답에 노출하지 않습니다.
            throw new InvalidBearerTokenException("Firebase ID token is invalid", exception);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
