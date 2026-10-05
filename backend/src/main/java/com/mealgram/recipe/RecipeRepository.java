package com.mealgram.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

// 레시피 조회/저장 인터페이스

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

}
