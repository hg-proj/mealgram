package com.mealgram.meal.nutrition;

import java.math.BigDecimal;

import com.mealgram.member.Member.ActivityLevel;
import com.mealgram.member.Member.Gender;

// 목표 열량 계산

public final class EnergyCalculator {

    public static final int DEFAULT_DAILY_CALORIE = 2000;

    private static final int MEALS_PER_DAY = 3;
    private static final int ADULT_MIN_AGE = 19;
    private static final int CHILD_MIN_AGE = 3;
    private static final int DIET_ADJUST = -500;
    private static final int BULK_ADJUST = 300;
    private static final String DIET_GOAL = "다이어트";
    private static final String BULK_GOAL = "벌크업";

    private EnergyCalculator() {

    }

    public static int dailyCalorie(Integer age, Gender gender, BigDecimal heightCm, BigDecimal weightKg,
                                   ActivityLevel activityLevel) {

        if (age == null || gender == null || age < CHILD_MIN_AGE) {
            return DEFAULT_DAILY_CALORIE;
        }
        if (heightCm == null || weightKg == null || activityLevel == null) {
            return referenceCalorie(age, gender);
        }

        double height = heightCm.doubleValue() / 100;
        double weight = weightKg.doubleValue();
        boolean male = gender == Gender.MALE;
        double eer;

        if (age >= ADULT_MIN_AGE) {
            double pa = adultPa(male, activityLevel);
            eer = male
                    ? 662 - 9.53 * age + pa * (15.91 * weight + 539.6 * height)
                    : 354 - 6.91 * age + pa * (9.36 * weight + 726 * height);
        } else {
            double pa = childPa(male, activityLevel);
            double growth = age <= 8 ? 20 : 25;
            eer = male
                    ? 88.5 - 61.9 * age + pa * (26.7 * weight + 903 * height) + growth
                    : 135.3 - 30.8 * age + pa * (10.0 * weight + 934 * height) + growth;
        }

        return (int) Math.round(eer);

    }

    public static int mealCalorie(int dailyCalorie, String goal) {

        return (int) Math.round((dailyCalorie + goalAdjust(goal)) / (double) MEALS_PER_DAY);

    }

    private static int goalAdjust(String goal) {

        if (DIET_GOAL.equals(goal)) {
            return DIET_ADJUST;
        }
        if (BULK_GOAL.equals(goal)) {
            return BULK_ADJUST;
        }

        return 0;

    }

    private static int referenceCalorie(int age, Gender gender) {

        if (age < ADULT_MIN_AGE) {
            return DEFAULT_DAILY_CALORIE;
        }

        boolean male = gender == Gender.MALE;
        if (age < 30) {
            return male ? 2600 : 2000;
        }
        if (age < 50) {
            return male ? 2500 : 1900;
        }
        if (age < 65) {
            return male ? 2200 : 1700;
        }
        if (age < 75) {
            return male ? 2000 : 1600;
        }

        return male ? 1900 : 1500;

    }

    private static double adultPa(boolean male, ActivityLevel level) {

        return switch (level) {
            case LOW -> 1.00;
            case LIGHT -> male ? 1.11 : 1.12;
            case MODERATE -> male ? 1.25 : 1.27;
            case HIGH -> male ? 1.48 : 1.45;
        };

    }

    private static double childPa(boolean male, ActivityLevel level) {

        return switch (level) {
            case LOW -> 1.00;
            case LIGHT -> male ? 1.13 : 1.16;
            case MODERATE -> male ? 1.26 : 1.31;
            case HIGH -> male ? 1.42 : 1.56;
        };

    }

}
