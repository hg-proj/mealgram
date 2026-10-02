package com.mealgram.ingredient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// 전체재료 조회 인터페이스

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Page<Ingredient> findByNameContaining(String keyword, Pageable pageable);

}
