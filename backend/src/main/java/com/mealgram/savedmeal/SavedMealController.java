package com.mealgram.savedmeal;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mealgram.savedmeal.dto.SavedMealCreateResponse;
import com.mealgram.savedmeal.dto.SavedMealRequest;
import com.mealgram.savedmeal.dto.SavedMealResponse;

import jakarta.validation.Valid;

// 내 식단 저장, 목록 조회, 삭제 API 엔드포인트

@RestController
@RequestMapping("/saved-meals")
public class SavedMealController {

    private final SavedMealService savedMealService;

    public SavedMealController(SavedMealService savedMealService) {
        this.savedMealService = savedMealService;
    }

    @PostMapping
    public ResponseEntity<SavedMealCreateResponse> save(@AuthenticationPrincipal Long memberId,
                                                        @Valid @RequestBody SavedMealRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMealService.save(memberId, request));

    }

    @GetMapping
    public ResponseEntity<List<SavedMealResponse>> getSavedMeals(
            @AuthenticationPrincipal Long memberId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(savedMealService.getSavedMeals(memberId, pageable));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long memberId, @PathVariable Long id) {

        savedMealService.delete(memberId, id);

        return ResponseEntity.noContent().build();

    }

}
