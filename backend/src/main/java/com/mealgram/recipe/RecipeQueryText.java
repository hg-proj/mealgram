package com.mealgram.recipe;

import java.util.ArrayList;
import java.util.List;

// 검색 조건을 임베딩용 질문 문장으로 변환

public final class RecipeQueryText {

    private static final String INGREDIENTS_FORMAT = "재료: %s";
    private static final String GOAL_FORMAT = "목표: %s";
    private static final String GENRE_FORMAT = "장르: %s";
    private static final String PART_SEPARATOR = " / ";
    private static final String INGREDIENT_SEPARATOR = ", ";

    private RecipeQueryText() {

    }

    public static String of(List<String> ingredients, String goal, String genre) {

        List<String> parts = new ArrayList<>();
        if (!ingredients.isEmpty()) {
            parts.add(String.format(INGREDIENTS_FORMAT, String.join(INGREDIENT_SEPARATOR, ingredients)));
        }
        if (goal != null && !goal.isBlank()) {
            parts.add(String.format(GOAL_FORMAT, goal.strip()));
        }
        if (genre != null && !genre.isBlank()) {
            parts.add(String.format(GENRE_FORMAT, genre.strip()));
        }

        return String.join(PART_SEPARATOR, parts);

    }

}
