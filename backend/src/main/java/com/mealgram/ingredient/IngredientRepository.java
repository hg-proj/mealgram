package com.mealgram.ingredient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 전체재료 조회 인터페이스

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    @Query("""
            select i from Ingredient i
            where i.name like concat('%', :keyword, '%')
              and i.category <> :excludedCategory
              and (select count(ri) from RecipeIngredient ri where ri.ingredient = i) >= :minRecipeCount
            """)
    Page<Ingredient> searchSelectable(@Param("keyword") String keyword,
                                      @Param("excludedCategory") String excludedCategory,
                                      @Param("minRecipeCount") long minRecipeCount,
                                      Pageable pageable);

}
