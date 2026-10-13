package com.mealgram.ingredient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.mealgram.common.exception.GlobalExceptionHandler;

class IngredientControllerTest {

    private IngredientService ingredientService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        ingredientService = mock(IngredientService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new IngredientController(ingredientService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(7L, null, List.of()));

    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();

    }

    @Test
    @DisplayName("재료 검색은 정렬을 보내지 않으면 이름순으로 조회한다.")
    void searchSortsByNameByDefault() throws Exception {

        when(ingredientService.search(anyString(), any())).thenReturn(List.of());

        mockMvc.perform(get("/ingredients").param("keyword", "두부")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(ingredientService).search(eq("두부"), pageable.capture());
        assertEquals(Sort.by("name"), pageable.getValue().getSort());

    }

    @Test
    @DisplayName("허용하지 않은 필드로 정렬을 요청하면 400 공통 오류 형식으로 응답한다.")
    void rejectsUnknownSortProperty() throws Exception {

        mockMvc.perform(get("/ingredients").param("sort", "password,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
        mockMvc.perform(get("/members/me/ingredients").param("sort", "ingredient.category"))
                .andExpect(status().isBadRequest());

        verify(ingredientService, never()).search(anyString(), any());
        verify(ingredientService, never()).getMyIngredients(any(), any());

    }

    @Test
    @DisplayName("내 재료 목록은 정렬을 보내지 않으면 최근 등록순으로 조회한다.")
    void myIngredientsSortsByLatestByDefault() throws Exception {

        when(ingredientService.getMyIngredients(eq(7L), any())).thenReturn(List.of());

        mockMvc.perform(get("/members/me/ingredients")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(ingredientService).getMyIngredients(eq(7L), pageable.capture());
        assertEquals(Sort.by(Sort.Direction.DESC, "id"), pageable.getValue().getSort());

    }

}
