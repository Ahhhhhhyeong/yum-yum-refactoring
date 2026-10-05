package com.yumyum.backend.user;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import com.yumyum.backend.auth.SignupRequest;

@Entity
@Table(name = "users")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    private String firebaseUid;
    @Column(nullable = false, length = 320)
    private String email;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, length = 10)
    private String gender;
    @Column(name = "birth_year", nullable = false)
    private Short birthYear;
    @Column(name = "height_cm", nullable = false, precision = 5, scale = 1)
    private BigDecimal height;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserProfile() {}

    public UserProfile(String firebaseUid, SignupRequest request) {
        this.firebaseUid = firebaseUid;
        this.email = request.email();
        this.name = request.name();
        this.gender = request.gender();
        this.birthYear = request.birthYear().shortValue();
        this.height = BigDecimal.valueOf(request.height());
    }

    @PrePersist
    void initializeTimestamps() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void updateTimestamp() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getFirebaseUid() { return firebaseUid; }
    public String getName() { return name; }
    public Short getBirthYear() { return birthYear; }
}
