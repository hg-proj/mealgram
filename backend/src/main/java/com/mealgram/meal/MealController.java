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
import com.mealgram.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// 식단 추천 요청, 결과 재조회, 랜덤 식단 API 엔드포인트

@Tag(name = "식단")
@RestController
@RequestMapping("/meals")
public class MealController {

    private final MealService mealService;
    private final MealSampleService mealSampleService;

    public MealController(MealService mealService, MealSampleService mealSampleService) {
        this.mealService = mealService;
        this.mealSampleService = mealSampleService;
    }

    @Operation(summary = "식단 추천")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @PostMapping
    public ResponseEntity<MealResponse> recommend(@Valid @RequestBody MealRequest request,
                                                  @AuthenticationPrincipal Long memberId) {

        MealResponse response = memberId == null
                ? mealService.recommendForGuest(request.requiredId(), request.subIds(), request.ingredientId(),
                        request.goal(), request.genre(), request.style(), request.bodyInfo())
                : mealService.recommend(memberId, request.requiredId(), request.subIds(), request.ingredientId(),
                        request.goal(), request.genre(), request.style(), request.bodyInfo());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(summary = "추천 식단 샘플 조회")
    @GetMapping("/samples")
    public ResponseEntity<List<MealSample>> samples() {

        return ResponseEntity.ok(mealSampleService.samples());

    }

    @Operation(summary = "식단 추천 결과 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @GetMapping("/{id}")
    public ResponseEntity<MealResponse> get(@PathVariable String id, @AuthenticationPrincipal Long memberId) {

        return ResponseEntity.ok(mealService.get(memberId, id));

    }

}
