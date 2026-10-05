package com.mealgram.ingredient;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.ingredient.dto.IngredientSearchResponse;
import com.mealgram.ingredient.dto.MyIngredientCreateResponse;
import com.mealgram.ingredient.dto.MyIngredientRequest;
import com.mealgram.ingredient.dto.MyIngredientResponse;
import com.mealgram.member.Member;
import com.mealgram.member.MemberRepository;

// 재료 검색, 내 재료 등록/조회/삭제 처리 서비스

@Service
public class IngredientService {

    private static final String EXCLUDED_CATEGORY = "양념";
    private static final long MIN_RECIPE_COUNT = 2;

    private final IngredientRepository ingredientRepository;
    private final MemberIngredientRepository memberIngredientRepository;
    private final MemberRepository memberRepository;

    public IngredientService(IngredientRepository ingredientRepository,
                             MemberIngredientRepository memberIngredientRepository,
                             MemberRepository memberRepository) {
        this.ingredientRepository = ingredientRepository;
        this.memberIngredientRepository = memberIngredientRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<IngredientSearchResponse> search(String keyword, Pageable pageable) {

        return ingredientRepository.searchSelectable(keyword, EXCLUDED_CATEGORY, MIN_RECIPE_COUNT, pageable)
                .map(IngredientSearchResponse::from)
                .getContent();

    }

    @Transactional(readOnly = true)
    public List<MyIngredientResponse> getMyIngredients(Long memberId, Pageable pageable) {

        return memberIngredientRepository.findByMemberId(memberId, pageable)
                .map(MyIngredientResponse::from)
                .getContent();

    }

    @Transactional
    public MyIngredientCreateResponse addMyIngredient(Long memberId, MyIngredientRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));

        if (memberIngredientRepository.existsByMemberIdAndIngredientId(memberId, ingredient.getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_MY_INGREDIENT);
        }

        MemberIngredient memberIngredient = MemberIngredient.builder()
                .member(member)
                .ingredient(ingredient)
                .build();

        memberIngredientRepository.save(memberIngredient);

        return new MyIngredientCreateResponse(memberIngredient.getId());
    }

    @Transactional
    public void deleteMyIngredient(Long memberId, Long memberIngredientId) {

        MemberIngredient memberIngredient = memberIngredientRepository.findByIdAndMemberId(memberIngredientId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MY_INGREDIENT_NOT_FOUND));

        memberIngredientRepository.delete(memberIngredient);
    }

}
