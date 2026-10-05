package com.mealgram.meal.dto;

import java.util.List;

// 식단 추천 결과 응답 데이터

public record MealResponse(String id, boolean relaxed, List<MealCandidate> candidates) {

}
