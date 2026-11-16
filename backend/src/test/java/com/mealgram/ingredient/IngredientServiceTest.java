package com.mealgram.ingredient;

import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.ingredient.dto.IngredientCatalogResponse;
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
    @DisplayName("재료를 분류별로 묶고 정해진 분류 순서대로 돌려준다.")
    void groupsCatalogByCategory() {

        when(ingredientRepository.searchSelectable(anyString(), anyCollection(), anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(
                        Ingredient.builder().id(1L).name("고등어").category("해산물").build(),
                        Ingredient.builder().id(2L).name("당근").category("채소").build(),
                        Ingredient.builder().id(3L).name("양파").category("채소").build(),
                        Ingredient.builder().id(4L).name("새로운재료").category("새분류").build())));

        List<IngredientCatalogResponse> catalog = service.getCatalog();

        assertEquals(List.of("채소", "해산물", "새분류"), catalog.stream().map(IngredientCatalogResponse::category).toList());
        assertEquals(2, catalog.get(0).ingredients().size());
        assertEquals("당근", catalog.get(0).ingredients().get(0).name());

    }

    @Test
    @DisplayName("분류 목록에는 양념과 도구를 제외하고 요리에 3번 이상 쓰인 재료만 조회한다.")
    void catalogQueriesOnlySelectableIngredients() {

        when(ingredientRepository.searchSelectable(anyString(), anyCollection(), anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getCatalog();

        verify(ingredientRepository).searchSelectable(eq(""), eq(List.of("양념", "도구")), eq(3L), any(Pageable.class));

    }

    @Test
    @DisplayName("없는 회원은 404 오류로 알린다.")
    void rejectsUnknownMember() {

        when(memberRepository.findById(2L)).thenReturn(Optional.empty());

        assertEquals(ErrorCode.MEMBER_NOT_FOUND,
                errorOf(() -> service.addMyIngredient(2L, new MyIngredientRequest(5L))));

    }

    @Test
    @DisplayName("분류 목록은 한 번 조회하면 잠시 보관해 두고 다시 조회하지 않는다.")
    void cachesCatalog() {

        when(ingredientRepository.searchSelectable(anyString(), anyCollection(), anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(Ingredient.builder().id(1L).name("당근").category("채소").build())));

        List<IngredientCatalogResponse> first = service.getCatalog();

        assertEquals(first, service.getCatalog());
        verify(ingredientRepository, times(1))
                .searchSelectable(anyString(), anyCollection(), anyLong(), any(Pageable.class));

    }

    @Test
    @DisplayName("검색어의 퍼센트와 밑줄과 역슬래시는 글자 그대로 찾는다.")
    void escapesLikeCharacters() {

        when(ingredientRepository.searchSelectable(anyString(), anyCollection(), anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.search("a%b_c\\d", Pageable.unpaged());

        verify(ingredientRepository).searchSelectable(eq("a\\%b\\_c\\\\d"), anyCollection(), anyLong(), any(Pageable.class));

    }

}
