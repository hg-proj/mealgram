package com.mealgram.dish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DishTest {

    private Dish dish(String name, DishRole role, String ingredients, String flags) {

        return Dish.builder().name(name).role(role).mainIngredients(ingredients).methods("").flags(flags)
                .calorie(BigDecimal.TEN).protein(BigDecimal.ONE).fat(BigDecimal.ONE).carbohydrate(BigDecimal.ONE).build();

    }

    @Test
    @DisplayName("쉼표로 이어 저장한 주재료와 특성을 목록으로 읽는다.")
    void readsStoredLists() {

        Dish dish = dish("닭가슴살구이", DishRole.MAIN, "닭고기", "저지방고단백,매운맛");

        assertEquals(List.of("닭고기"), dish.mainIngredientList());
        assertTrue(dish.hasFlag(Dish.SPICY));
        assertFalse(dish.hasFlag(Dish.FRIED));

    }

    @Test
    @DisplayName("저장한 값이 비어 있으면 빈 목록으로 읽는다.")
    void readsEmptyLists() {

        Dish dish = dish("시금치나물", DishRole.SIDE, "", "");

        assertTrue(dish.mainIngredientList().isEmpty());
        assertTrue(dish.methodList().isEmpty());
        assertFalse(dish.hasFlag(Dish.FRIED));

    }

    @Test
    @DisplayName("밥이나 면이 이름에 들어간 국은 주식 겸용으로 본다.")
    void detectsStapleSoup() {

        assertTrue(dish("돼지국밥", DishRole.SOUP, "", "").isStapleSoup());
        assertTrue(dish("김치수제비국", DishRole.SOUP, "", "").isStapleSoup());
        assertFalse(dish("미역국", DishRole.SOUP, "", "").isStapleSoup());
        assertFalse(dish("김치밥", DishRole.RICE, "", "").isStapleSoup());

    }

    @Test
    @DisplayName("김치류와 단무지나 피클 같은 반찬은 절임으로 센다.")
    void detectsPickled() {

        assertTrue(dish("배추김치", DishRole.PICKLE, "", "").isPickled());
        assertTrue(dish("단무지무침", DishRole.SIDE, "", "").isPickled());
        assertFalse(dish("시금치나물", DishRole.SIDE, "", "").isPickled());

    }

    @Test
    @DisplayName("불고기는 재료가 달라도 같은 계열이고 일품은 종류별로 한 계열이다.")
    void groupsSimilarDishesIntoFamilies() {

        Dish beef = dish("소불고기", DishRole.MAIN, "소고기", "");
        Dish sprouts = dish("콩나물불고기", DishRole.MAIN, "돼지고기", "");
        Dish mackerelGrill = dish("고등어구이", DishRole.MAIN, "생선", "");
        Dish mackerelBraise = dish("고등어김치조림", DishRole.MAIN, "생선", "");
        Dish tofu = dish("두부조림", DishRole.MAIN, "두부콩", "");
        Dish noodle1 = dish("잔치국수", DishRole.ONE_DISH, "", Dish.NOODLE);
        Dish noodle2 = dish("자장면", DishRole.ONE_DISH, "", Dish.NOODLE);
        Dish curry = dish("카레라이스", DishRole.ONE_DISH, "", Dish.RICE_BOWL);

        assertTrue(beef.families().stream().anyMatch(sprouts.families()::contains));
        assertTrue(mackerelGrill.families().stream().anyMatch(mackerelBraise.families()::contains));
        assertFalse(beef.families().stream().anyMatch(tofu.families()::contains));
        assertFalse(mackerelGrill.families().stream().anyMatch(tofu.families()::contains));
        assertTrue(noodle1.families().stream().anyMatch(noodle2.families()::contains));
        assertFalse(noodle1.families().stream().anyMatch(curry.families()::contains));

    }

    @Test
    @DisplayName("이름이나 주재료에 찾는 재료가 들어 있으면 맞는 요리로 본다.")
    void matchesRequestedIngredient() {

        Dish tofu = dish("두부조림", DishRole.MAIN, "두부콩", "");
        Dish chicken = dish("닭가슴살구이", DishRole.MAIN, "닭고기", "");

        assertTrue(tofu.matchesAny(List.of("두부")));
        assertTrue(chicken.matchesAny(List.of("닭고기")));
        assertFalse(chicken.matchesAny(List.of("두부", "연어")));

    }

    @Test
    @DisplayName("요청한 재료가 요리의 재료 목록에 있으면 이름에 없어도 맞는 요리로 본다.")
    void matchesIngredientListNotOnlyName() {

        Dish dish = Dish.builder().name("제육볶음").role(DishRole.MAIN).mainIngredients("돼지고기").methods("볶음").flags("")
                .ingredients("돼지고기,양파,당근").calorie(BigDecimal.TEN).protein(BigDecimal.ONE).fat(BigDecimal.ONE)
                .carbohydrate(BigDecimal.ONE).build();

        assertEquals(List.of("돼지고기", "양파", "당근"), dish.ingredientList());
        assertTrue(dish.matchesAny(List.of("양파")));
        assertFalse(dish.matchesAny(List.of("연어")));

    }

    @Test
    @DisplayName("특수 양념은 재료 맞추기에 쓰이지 않고 따로 읽는다.")
    void seasoningsAreNotIngredients() {

        Dish dish = Dish.builder().name("자장면").role(DishRole.ONE_DISH).mainIngredients("").methods("").flags("면")
                .ingredients("국수,돼지고기,양파").seasonings("춘장").calorie(BigDecimal.TEN).protein(BigDecimal.ONE)
                .fat(BigDecimal.ONE).carbohydrate(BigDecimal.ONE).build();

        assertEquals(List.of("춘장"), dish.seasoningList());
        assertFalse(dish.matchesAny(List.of("춘장")));
        assertTrue(dish.matchesAny(List.of("양파")));

    }

}
