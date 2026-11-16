package com.mealgram.ingredient;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 재료 조회 인터페이스

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findByNameIn(Collection<String> names);

    @Query("""
            select i from Ingredient i
            where i.name like concat('%', :keyword, '%') escape '\\'
              and i.category not in :excludedCategories
              and (
                  select count(d.id) from Dish d
                  where d.role <> com.mealgram.dish.DishRole.DESSERT
                    and concat(',', d.ingredients, ',') like concat('%,', i.name, ',%')) >= :minDishCount
            """)
    Page<Ingredient> searchSelectable(@Param("keyword") String keyword,
                                      @Param("excludedCategories") Collection<String> excludedCategories,
                                      @Param("minDishCount") long minDishCount,
                                      Pageable pageable);

}
