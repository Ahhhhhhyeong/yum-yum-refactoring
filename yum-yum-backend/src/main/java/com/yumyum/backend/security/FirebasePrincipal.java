package com.yumyum.backend.security;

import java.security.Principal;

public record FirebasePrincipal(String uid, String email) implements Principal {
    @Override
    public String getName() {
        return uid;
    }
}
