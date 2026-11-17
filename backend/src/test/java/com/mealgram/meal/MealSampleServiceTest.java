package com.mealgram.meal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.dish.Dish;
import com.mealgram.dish.DishPhotos;
import com.mealgram.dish.DishRole;
import com.mealgram.meal.dto.MealResponse;
import com.mealgram.meal.dto.MealSample;

class MealSampleServiceTest {

    private DishComposer dishComposer;
    private DishPhotos dishPhotos;
    private MealSampleService service;

    @BeforeEach
    void setUp() {

        dishComposer = mock(DishComposer.class);
        dishPhotos = mock(DishPhotos.class);
        MealService mealService = mock(MealService.class);
        when(mealService.storeShared(any())).thenReturn(new MealResponse("sample-id", false, List.of()));
        service = new MealSampleService(dishComposer, dishPhotos, mealService);

    }

    private ComposedMeal meal(String mainName) {

        return DishFixtures.meal(true,
                DishFixtures.dish("쌀밥", DishRole.RICE, 300, 6, 1, 65),
                DishFixtures.dish(mainName, DishRole.MAIN, 250, 20, 12, 15));

    }

    @Test
    @DisplayName("다이어트, 유지, 벌크업 순서로 식단 세 개를 만든다.")
    void makesOnePerGoal() {

        when(dishComposer.compose(any(MealCondition.class), anyInt(), any())).thenReturn(List.of(meal("가")));

        List<MealSample> samples = service.samples();

        assertEquals(List.of("다이어트", "유지", "벌크업"), samples.stream().map(MealSample::goal).toList());
        assertEquals(2, samples.get(0).dishes().size());

    }

    @Test
    @DisplayName("사진이 있는 요리가 든 식단을 먼저 고른다.")
    void prefersMealWithPhoto() {

        ComposedMeal plain = meal("사진없음");
        ComposedMeal photographed = meal("사진있음");
        when(dishComposer.compose(any(MealCondition.class), anyInt(), any())).thenReturn(List.of(plain, photographed));
        when(dishPhotos.urlOf(any(Dish.class))).thenAnswer(invocation ->
                "사진있음".equals(((Dish) invocation.getArgument(0)).getName()) ? "/images/dishes/g01.webp" : null);

        MealSample sample = service.samples().get(0);

        assertEquals("사진있음", sample.dishes().get(1).name());
        assertEquals("/images/dishes/g01.webp", sample.dishes().get(1).imageUrl());

    }

    @Test
    @DisplayName("사진이 있는 식단이 없으면 첫 식단을 쓴다.")
    void usesFirstWithoutPhoto() {

        when(dishComposer.compose(any(MealCondition.class), anyInt(), any())).thenReturn(List.of(meal("가"), meal("나")));

        assertEquals("가", service.samples().get(0).dishes().get(1).name());

    }

    @Test
    @DisplayName("구성할 수 있는 식단이 없으면 예외가 난다.")
    void throwsWhenEmpty() {

        when(dishComposer.compose(any(MealCondition.class), anyInt(), any())).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.samples());

        assertEquals(ErrorCode.MEAL_CANDIDATE_NOT_FOUND, exception.getErrorCode());

    }

}
