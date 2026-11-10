package com.mealgram.recipe;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mealgram.recipe.dto.RecipeResponse;

// 레시피 상세, 부족 재료 조회 API 엔드포인트

@RestController
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/recipes/{id}")
    public ResponseEntity<RecipeResponse> getDetail(@PathVariable Long id,
                                                    @RequestParam(required = false) List<Long> ingredientIds,
                                                    @AuthenticationPrincipal Long memberId) {

        return ResponseEntity.ok(memberId == null
                ? recipeService.getDetailForGuest(id, ingredientIds)
                : recipeService.getDetail(id, memberId));

    }

}
