package com.mealgram.recipe;

import java.util.List;

// 임베딩에 넣을 레시피 문장 생성

public final class RecipeEmbeddingText {

    private static final String FORMAT = "이름: %s / 분류: %s / 조리방법: %s / 재료: %s";
    private static final String INGREDIENT_SEPARATOR = ", ";

    private RecipeEmbeddingText() {

    }

    public static String of(String name, String category, String cookingMethod, List<String> ingredients) {

        return String.format(FORMAT, name, category, cookingMethod, String.join(INGREDIENT_SEPARATOR, ingredients));

    }

}
