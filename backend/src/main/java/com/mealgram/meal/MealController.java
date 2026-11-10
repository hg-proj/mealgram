package com.mealgram.meal;

import java.util.List;

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
import com.mealgram.meal.dto.MealSample;

import jakarta.validation.Valid;

// 식단 추천 요청, 결과 재조회, 랜덤 식단 API 엔드포인트

@RestController
@RequestMapping("/meals")
public class MealController {

    private final MealService mealService;
    private final MealSampleService mealSampleService;

    public MealController(MealService mealService, MealSampleService mealSampleService) {
        this.mealService = mealService;
        this.mealSampleService = mealSampleService;
    }

    @PostMapping
    public ResponseEntity<MealResponse> recommend(@Valid @RequestBody MealRequest request,
                                                  @AuthenticationPrincipal Long memberId) {

        MealResponse response = memberId == null
                ? mealService.recommendForGuest(request.requiredId(), request.subIds(), request.ingredientId(),
                        request.goal(), request.genre(), request.bodyInfo())
                : mealService.recommend(memberId, request.requiredId(), request.subIds(), request.ingredientId(),
                        request.goal(), request.genre());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/samples")
    public ResponseEntity<List<MealSample>> samples() {

        return ResponseEntity.ok(mealSampleService.samples());

    }

    @GetMapping("/{id}")
    public ResponseEntity<MealResponse> get(@PathVariable String id, @AuthenticationPrincipal Long memberId) {

        return ResponseEntity.ok(mealService.get(memberId, id));

    }

}
