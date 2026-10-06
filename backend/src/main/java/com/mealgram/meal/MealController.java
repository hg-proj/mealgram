package com.mealgram.meal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mealgram.meal.dto.MealRequest;
import com.mealgram.meal.dto.MealResponse;

import jakarta.validation.Valid;

// 식단 추천 요청, 결과 재조회 API 엔드포인트

@RestController
@RequestMapping("/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping
    public ResponseEntity<MealResponse> recommend(@Valid @RequestBody MealRequest request,
                                                  @AuthenticationPrincipal Long memberId) {

        MealResponse response = mealService.recommend(memberId, request.requiredId(), request.subIds(),
                request.ingredientId(), request.goal(), request.genre());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<MealResponse> get(@PathVariable String id) {

        return ResponseEntity.ok(mealService.get(id));

    }

}
