package com.mealgram.recipe.dto;

import java.util.List;

// 레시피 검색 결과 후보와 조건 완화 여부

public record RecipeSearchResult(List<RecipeCandidate> candidates, boolean relaxed) {

}
