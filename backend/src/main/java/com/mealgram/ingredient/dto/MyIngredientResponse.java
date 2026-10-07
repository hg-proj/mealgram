package com.mealgram.ingredient.dto;

import com.mealgram.ingredient.MemberIngredient;

// 내 재료 목록 응답 데이터

public record MyIngredientResponse(Long id, Long ingredientId, String name) {

    public static MyIngredientResponse from(MemberIngredient memberIngredient) {

        return new MyIngredientResponse(memberIngredient.getId(), memberIngredient.getIngredient().getId(),
                memberIngredient.getIngredient().getName());

    }

}
