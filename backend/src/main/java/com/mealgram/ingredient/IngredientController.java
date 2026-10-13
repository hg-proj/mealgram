package com.mealgram.ingredient;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mealgram.common.validation.SortValidator;
import com.mealgram.ingredient.dto.IngredientSearchResponse;
import com.mealgram.ingredient.dto.MyIngredientCreateResponse;
import com.mealgram.ingredient.dto.MyIngredientRequest;
import com.mealgram.ingredient.dto.MyIngredientResponse;

import jakarta.validation.Valid;

// 재료 검색, 내 재료 등록/조회/삭제 API 엔드포인트

@RestController
public class IngredientController {

    private static final Set<String> SEARCH_SORT_PROPERTIES = Set.of("id", "name");
    private static final Set<String> MY_SORT_PROPERTIES = Set.of("id", "createdAt");

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientSearchResponse>> search(
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(sort = "name") Pageable pageable) {

        SortValidator.require(pageable, SEARCH_SORT_PROPERTIES);

        return ResponseEntity.ok(ingredientService.search(keyword, pageable));

    }

    @GetMapping("/members/me/ingredients")
    public ResponseEntity<List<MyIngredientResponse>> getMyIngredients(
            @AuthenticationPrincipal Long memberId,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        SortValidator.require(pageable, MY_SORT_PROPERTIES);

        return ResponseEntity.ok(ingredientService.getMyIngredients(memberId, pageable));

    }

    @PostMapping("/members/me/ingredients")
    public ResponseEntity<MyIngredientCreateResponse> addMyIngredient(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody MyIngredientRequest request) {

        MyIngredientCreateResponse response = ingredientService.addMyIngredient(memberId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @DeleteMapping("/members/me/ingredients/{id}")
    public ResponseEntity<Void> deleteMyIngredient(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long id) {

        ingredientService.deleteMyIngredient(memberId, id);

        return ResponseEntity.noContent().build();

    }

}
