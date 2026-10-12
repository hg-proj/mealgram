package com.mealgram.ingredient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.ingredient.dto.MyIngredientRequest;
import com.mealgram.member.Member;
import com.mealgram.member.MemberRepository;

class IngredientServiceTest {

    private IngredientRepository ingredientRepository;
    private MemberIngredientRepository memberIngredientRepository;
    private MemberRepository memberRepository;
    private IngredientService service;

    @BeforeEach
    void setUp() {

        ingredientRepository = mock(IngredientRepository.class);
        memberIngredientRepository = mock(MemberIngredientRepository.class);
        memberRepository = mock(MemberRepository.class);
        service = new IngredientService(ingredientRepository, memberIngredientRepository, memberRepository);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(Member.builder().id(1L).build()));
        when(ingredientRepository.findById(5L)).thenReturn(Optional.of(Ingredient.builder().id(5L).name("두부").build()));

    }

    private ErrorCode errorOf(Runnable action) {

        return assertThrows(BusinessException.class, action::run).getErrorCode();

    }

    @Test
    @DisplayName("이미 등록한 재료는 저장하지 않고 409 오류로 알린다.")
    void rejectsAlreadyRegistered() {

        when(memberIngredientRepository.existsByMemberIdAndIngredientId(1L, 5L)).thenReturn(true);

        assertEquals(ErrorCode.DUPLICATE_MY_INGREDIENT,
                errorOf(() -> service.addMyIngredient(1L, new MyIngredientRequest(5L))));
        verify(memberIngredientRepository, never()).save(any());

    }

    @Test
    @DisplayName("동시에 같은 재료를 등록해 유니크 제약에 걸려도 409 오류로 알린다.")
    void convertsUniqueViolationToDuplicate() {

        when(memberIngredientRepository.existsByMemberIdAndIngredientId(1L, 5L)).thenReturn(false);
        when(memberIngredientRepository.save(any())).thenThrow(new DataIntegrityViolationException("유니크 제약 위반"));

        assertEquals(ErrorCode.DUPLICATE_MY_INGREDIENT,
                errorOf(() -> service.addMyIngredient(1L, new MyIngredientRequest(5L))));

    }

    @Test
    @DisplayName("없는 재료는 404 오류로 알린다.")
    void rejectsUnknownIngredient() {

        when(ingredientRepository.findById(99L)).thenReturn(Optional.empty());

        assertEquals(ErrorCode.INGREDIENT_NOT_FOUND,
                errorOf(() -> service.addMyIngredient(1L, new MyIngredientRequest(99L))));

    }

    @Test
    @DisplayName("없는 회원은 404 오류로 알린다.")
    void rejectsUnknownMember() {

        when(memberRepository.findById(2L)).thenReturn(Optional.empty());

        assertEquals(ErrorCode.MEMBER_NOT_FOUND,
                errorOf(() -> service.addMyIngredient(2L, new MyIngredientRequest(5L))));

    }

}
