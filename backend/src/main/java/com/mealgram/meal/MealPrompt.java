package com.mealgram.meal;

import java.util.List;
import java.util.stream.Collectors;

import com.mealgram.recipe.dto.RecipeCandidate;

// 식단 생성 프롬프트 구성

public final class MealPrompt {

    private static final int MEAL_COUNT = 3;

    private static final String SYSTEM = """
            당신은 영양사입니다. 사용자가 준 후보 레시피 중에서만 골라 식단안을 만듭니다.
            규칙:
            - 반드시 후보 목록에 있는 id만 사용합니다.
            - 식단안 하나는 밥 1개, 국 또는 찌개 1개, 메인 1개, 반찬 2개, 김치 1개를 목표로 구성합니다. 후보에 없는 칸은 비워도 됩니다.
            - 식단안은 서로 다른 스타일로 %d개를 만듭니다. 예: 담백한 한식, 매콤한 맛, 저칼로리.
            - 같은 레시피를 한 식단안에 두 번 넣지 않습니다.
            - 후보에 조건충족 표시가 있으면 표시된 레시피를 우선 사용하고, 부족할 때만 나머지를 사용합니다.
            - 응답은 JSON 객체 하나만 출력합니다.
            응답 형식: {"meals":[{"style":"스타일 이름","reason":"한 줄 설명","recipeIds":[1,2,3]}]}
            """;

    private static final String USER = """
            목표: %s
            장르: %s
            후보 레시피:
            %s
            """;

    private static final String RECIPE_LINE =
            "id=%d | %s | %s | %skcal 탄수화물 %sg 단백질 %sg 지방 %sg | 재료: %s";
    private static final String MATCHED_MARK = " | 조건충족";
    private static final String NOT_SELECTED = "선택 안 함";

    private MealPrompt() {

    }

    public static String system() {

        return SYSTEM.formatted(MEAL_COUNT);

    }

    public static String user(List<RecipeCandidate> candidates, String goal, String genre) {

        boolean mixed = candidates.stream().anyMatch(candidate -> !candidate.matched());
        String recipes = candidates.stream()
                .map(candidate -> RECIPE_LINE.formatted(candidate.id(), candidate.name(), candidate.category(),
                        candidate.calorie().stripTrailingZeros().toPlainString(),
                        candidate.carbohydrate().stripTrailingZeros().toPlainString(),
                        candidate.protein().stripTrailingZeros().toPlainString(),
                        candidate.fat().stripTrailingZeros().toPlainString(),
                        String.join(", ", candidate.ingredients())) + (mixed && candidate.matched() ? MATCHED_MARK : ""))
                .collect(Collectors.joining("\n"));

        return USER.formatted(valueOrDefault(goal), valueOrDefault(genre), recipes);

    }

    private static String valueOrDefault(String value) {

        return value == null || value.isBlank() ? NOT_SELECTED : value.strip();

    }

}
