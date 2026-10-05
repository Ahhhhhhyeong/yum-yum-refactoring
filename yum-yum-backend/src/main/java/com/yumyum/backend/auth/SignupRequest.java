package com.yumyum.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.AssertTrue;

public record SignupRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 8, max = 20)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,20}$") String pw,
        @NotBlank @Pattern(regexp = "female|male") String gender,
        @NotNull @Min(1900) Integer birthYear,
        @NotNull @Min(50) @Max(250) Integer height,
        @NotNull @Min(20) @Max(300) Integer weight,
        @NotBlank @Pattern(regexp = "loss|gain|maintain") String goals,
        @Pattern(regexp = "^$|^(?:[2-9][0-9]|[12][0-9]{2}|300)(?:\\.0)?$|^(?:[2-9][0-9]|[12][0-9]{2})\\.[0-9]$") String targetWeight,
        @NotBlank @Pattern(regexp = "none|light|moderate|intense") String targetExercise,
        @AssertTrue boolean service,
        @AssertTrue boolean privacy,
        @AssertTrue boolean sensitive
) {
    @Override
    public String toString() {
        return "SignupRequest[email=" + email + ", pw=[REDACTED]]";
    }
}
