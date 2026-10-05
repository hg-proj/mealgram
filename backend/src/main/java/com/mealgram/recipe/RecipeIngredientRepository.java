package com.mealgram.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

// 레시피 재료 조회/저장 인터페이스

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

}
