package com.yumyum.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String pw,
        String gender,
        String age,
        String height,
        String weight,
        String goals,
        String targetWeight,
        String targetExercise,
        boolean service,
        boolean privacy,
        boolean sensitive
) {
    @Override
    public String toString() {
        return "SignupRequest[email=" + email + ", pw=[REDACTED]]";
    }
}
