package com.yumyum.backend;

import com.yumyum.backend.user.WaterIntakeCalculator;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class WaterIntakeCalculatorTests {
    @Test
    void preservesExistingRulesAtEveryAgeBoundaryForBothGenders() {
        int[][] cases = {
                {1, 1300, 1300}, {2, 1300, 1300},
                {3, 1400, 1400}, {5, 1400, 1400},
                {6, 1700, 1700}, {8, 1700, 1700},
                {9, 2100, 2100}, {11, 2100, 2100},
                {12, 2400, 2000}, {14, 2400, 2000},
                {15, 2700, 2000}, {18, 2700, 2000},
                {19, 2600, 2100}, {100, 2600, 2100}
        };
        for (int[] example : cases) {
            assertThat(WaterIntakeCalculator.calculate(example[0], "male"))
                    .as("male, age=%s", example[0]).isEqualTo(example[1]);
            assertThat(WaterIntakeCalculator.calculate(example[0], "female"))
                    .as("female, age=%s", example[0]).isEqualTo(example[2]);
        }
    }
}
