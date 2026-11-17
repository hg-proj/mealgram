package com.mealgram.shopping.dto;

import java.util.List;

// 장보기 목록 응답 데이터

public record ShoppingListResponse(List<ShoppingItem> toBuy, List<ShoppingItem> have, List<ShoppingItem> seasonings) {

}
