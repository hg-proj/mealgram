package com.mealgram.shopping.dto;

import java.util.List;

// 장보기 목록 한 줄, 재료나 양념 이름과 쓰이는 요리

public record ShoppingItem(String name, String category, List<String> usedIn) {

}
