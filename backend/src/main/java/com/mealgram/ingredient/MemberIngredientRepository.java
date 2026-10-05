package com.mealgram.ingredient;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 내 재료 조회/저장 인터페이스

public interface MemberIngredientRepository extends JpaRepository<MemberIngredient, Long> {

    @EntityGraph(attributePaths = "ingredient")
    Page<MemberIngredient> findByMemberId(Long memberId, Pageable pageable);

    boolean existsByMemberIdAndIngredientId(Long memberId, Long ingredientId);

    Optional<MemberIngredient> findByIdAndMemberId(Long id, Long memberId);

    @Query("select mi.ingredient.id from MemberIngredient mi where mi.member.id = :memberId")
    List<Long> findIngredientIdsByMemberId(@Param("memberId") Long memberId);
    
}
