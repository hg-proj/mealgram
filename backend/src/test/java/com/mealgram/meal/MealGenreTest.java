package com.mealgram.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.dish.Dish;
import com.mealgram.dish.DishRole;

class MealGenreTest {

    private Dish dish(String cuisine) {

        return DishFixtures.dish("요리", DishRole.SIDE, cuisine, 100, 5, 2, 10, List.of(), List.of(), List.of());

    }

    private String genreOf(String... cuisines) {

        Dish[] dishes = java.util.Arrays.stream(cuisines).map(this::dish).toArray(Dish[]::new);

        return MealGenre.of(DishFixtures.meal(true, dishes).dishes());

    }

    @Test
    @DisplayName("요리가 모두 한식이면 한식이라고 한다.")
    void allKorean() {

        assertEquals("한식", genreOf("한식", "한식", "한식"));

    }

    @Test
    @DisplayName("한식에 양식이 하나 섞여도 가장 많은 한식 하나만 말한다.")
    void majorityOnly() {

        assertEquals("한식", genreOf("양식", "한식", "한식"));

    }

    @Test
    @DisplayName("한식이 아닌 장르가 더 많으면 그 장르를 말한다.")
    void otherGenreWhenMore() {

        assertEquals("일식", genreOf("한식", "일식", "일식", "양식"));

    }

    @Test
    @DisplayName("개수가 같으면 한식을 말한다.")
    void tieGoesToKorean() {

        assertEquals("한식", genreOf("양식", "한식"));

    }

    private String genreOf(List<String> chosen, String... cuisines) {

        Dish[] dishes = java.util.Arrays.stream(cuisines).map(this::dish).toArray(Dish[]::new);

        return MealGenre.of(DishFixtures.meal(true, dishes).dishes(), chosen);

    }

    @Test
    @DisplayName("양식을 골랐고 양식 요리가 들었으면 한식이 더 많아도 양식이라고 한다.")
    void chosenGenreWins() {

        assertEquals("양식", genreOf(List.of("양식"), "양식", "한식", "한식", "한식"));

    }

    @Test
    @DisplayName("고른 장르의 요리가 하나도 없으면 가장 많은 장르를 말한다.")
    void chosenGenreAbsent() {

        assertEquals("한식", genreOf(List.of("양식"), "한식", "한식"));

    }

    @Test
    @DisplayName("한식과 양식을 함께 골랐으면 그중 더 많은 쪽을 말한다.")
    void chosenMore() {

        assertEquals("한식", genreOf(List.of("한식", "양식"), "양식", "한식", "한식"));

    }

    @Test
    @DisplayName("장르 정보가 없으면 비워 둔다.")
    void emptyWhenUnknown() {

        assertNull(MealGenre.of(List.of()));

    }

}
