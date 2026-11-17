package com.mealgram.shopping;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mealgram.shopping.dto.ShoppingListRequest;
import com.mealgram.shopping.dto.ShoppingListResponse;

import jakarta.validation.Valid;

// 장보기 목록 계산 API 엔드포인트

@RestController
@RequestMapping("/shopping-list")
public class ShoppingController {

    private final ShoppingService shoppingService;

    public ShoppingController(ShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }

    @PostMapping
    public ResponseEntity<ShoppingListResponse> calculate(@Valid @RequestBody ShoppingListRequest request,
                                                          @AuthenticationPrincipal Long memberId) {

        return ResponseEntity.ok(shoppingService.calculate(memberId, request));

    }

}
