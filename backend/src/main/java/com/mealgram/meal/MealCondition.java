package com.mealgram.meal;

import java.util.List;

// 식단 구성 조건

public record MealCondition(String goal,
                            DietStyle style,
                            int targetCalorie,
                            List<String> requiredNames,
                            List<String> preferredNames,
                            List<String> genres) {

    public MealCondition(String goal,
                         DietStyle style,
                         int targetCalorie,
                         List<String> requiredNames,
                         List<String> preferredNames) {

        this(goal, style, targetCalorie, requiredNames, preferredNames, List.of());

    }

}
