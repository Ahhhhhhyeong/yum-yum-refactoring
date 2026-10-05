package com.yumyum.backend.user;

/** 기존 프론트엔드 calculateWaterIntake의 연령·성별별 수분 목표(ml). */
public final class WaterIntakeCalculator {
    private WaterIntakeCalculator() {}

    public static int calculate(int age, String gender) {
        if (age >= 1 && age <= 2) return 1300;
        if (age >= 3 && age <= 5) return 1400;
        if (age >= 6 && age <= 8) return 1700;
        if (age >= 9 && age <= 11) return 2100;
        if (age >= 12 && age <= 14) return "male".equals(gender) ? 2400 : 2000;
        if (age >= 15 && age <= 18) return "male".equals(gender) ? 2700 : 2000;
        return "male".equals(gender) ? 2600 : 2100;
    }
}
