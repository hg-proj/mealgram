package com.mealgram.shopping.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

// 장보기 목록 요청 데이터

public record ShoppingListRequest(@NotEmpty @Size(max = 30) List<Long> dishIds,
                                  @Size(max = 200) List<Long> ownedIngredientIds) {

}
