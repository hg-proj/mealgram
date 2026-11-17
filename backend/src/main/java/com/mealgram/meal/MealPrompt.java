package com.mealgram.meal;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.mealgram.meal.nutrition.NutritionSummary;

// 식단 후보 선택 프롬프트 생성

public final class MealPrompt {

    public static final int MEAL_COUNT = 3;

    private static final String SYSTEM = """
            당신은 영양 규칙으로 미리 구성한 식단 후보 중에서 가장 자연스러운 식단을 고르고 이름을 붙이는 도우미입니다.
            규칙:
            - 반드시 후보 목록에 있는 번호만 사용합니다.
            - 서로 다른 식단 %d개를 고릅니다. 메인 반찬과 주재료와 조리법이 서로 다르게 고릅니다.
            - 요리의 어울림을 봅니다. 맛이 겹치거나 무거운 조합, 어색한 조합은 피합니다.
            - style은 12자 이내의 식단 이름(예: 담백한 한식 한 상)으로 씁니다.
            - 기준 미달로 표시된 후보는 영양 비율이 식단 성향에서 벗어난 것이므로, 표시가 없는 후보를 우선 고릅니다.
            - 영양 수치는 바꾸지 않고 후보에 적힌 값만 참고합니다.
            - 응답은 JSON 객체 하나만 출력합니다.
            응답 형식: {"meals":[{"index":0,"style":"식단 이름"}]}
            """;

    private static final String USER = """
            목표: %s
            식단 성향: %s
            장르: %s
            한 끼 목표 열량: %dkcal
            후보 식단:
            %s
            """;

    private static final String MEAL_LINE = "번호=%d | %s | %skcal 탄수화물 %s%% 단백질 %s%% 지방 %s%%";
    private static final String DISH_FORMAT = "%s %dg";
    private static final String BELOW_STANDARD_MARK = " | 기준 미달";
    private static final String NOT_SELECTED = "선택 안 함";

    private MealPrompt() {

    }

    public static String system() {

        return SYSTEM.formatted(MEAL_COUNT);

    }

    public static String user(List<ComposedMeal> meals, String goal, DietStyle style, String genre, int targetCalorie) {

        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < meals.size(); i++) {
            ComposedMeal meal = meals.get(i);
            NutritionSummary nutrition = MealNutrition.summarize(meal, targetCalorie, style);
            String dishes = meal.dishes().stream()
                    .map(composed -> DISH_FORMAT.formatted(composed.dish().getName(), composed.grams()))
                    .collect(Collectors.joining(", "));
            lines.append(MEAL_LINE.formatted(i, dishes, plain(nutrition.calorie()), plain(nutrition.carbohydrateRatio()),
                    plain(nutrition.proteinRatio()), plain(nutrition.fatRatio()))).append('\n');
        }

        return USER.formatted(valueOrDefault(goal), style.getLabel(), valueOrDefault(genre), targetCalorie, lines);

    }

    private static String plain(BigDecimal value) {

        return value.stripTrailingZeros().toPlainString();

    }

    private static String valueOrDefault(String value) {

        return value == null || value.isBlank() ? NOT_SELECTED : value.strip();

    }

}
