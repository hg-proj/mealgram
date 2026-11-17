package com.mealgram.savedmeal;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// 저장식단 조회/저장 인터페이스

public interface SavedMealRepository extends JpaRepository<SavedMeal, Long> {

    Page<SavedMeal> findByMemberId(Long memberId, Pageable pageable);

    boolean existsByMemberIdAndMealIdAndCandidateIndex(Long memberId, String mealId, int candidateIndex);

    Optional<SavedMeal> findByIdAndMemberId(Long id, Long memberId);

}
