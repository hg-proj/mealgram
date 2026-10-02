package com.mealgram.ingredient;

import java.util.List;

import org.springframework.data.domain.Pageable;
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

import com.mealgram.ingredient.dto.IngredientSearchResponse;
import com.mealgram.ingredient.dto.MyIngredientCreateResponse;
import com.mealgram.ingredient.dto.MyIngredientRequest;
import com.mealgram.ingredient.dto.MyIngredientResponse;

import jakarta.validation.Valid;

// 재료 검색, 내 재료 등록/조회/삭제 API 엔드포인트

@RestController
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientSearchResponse>> search(
            @RequestParam(defaultValue = "") String keyword,
            Pageable pageable) {

        return ResponseEntity.ok(ingredientService.search(keyword, pageable));

    }

    @GetMapping("/members/me/ingredients")
    public ResponseEntity<List<MyIngredientResponse>> getMyIngredients(
            @AuthenticationPrincipal Long memberId,
            Pageable pageable) {

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
