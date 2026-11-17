package com.mealgram.shopping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.dish.Dish;
import com.mealgram.dish.DishRepository;
import com.mealgram.dish.DishRole;
import com.mealgram.ingredient.Ingredient;
import com.mealgram.ingredient.IngredientRepository;
import com.mealgram.ingredient.MemberIngredientRepository;
import com.mealgram.shopping.dto.ShoppingItem;
import com.mealgram.shopping.dto.ShoppingListRequest;
import com.mealgram.shopping.dto.ShoppingListResponse;

class ShoppingServiceTest {

    private DishRepository dishRepository;
    private IngredientRepository ingredientRepository;
    private MemberIngredientRepository memberIngredientRepository;
    private ShoppingService service;

    @BeforeEach
    void setUp() {

        dishRepository = mock(DishRepository.class);
        ingredientRepository = mock(IngredientRepository.class);
        memberIngredientRepository = mock(MemberIngredientRepository.class);
        service = new ShoppingService(dishRepository, ingredientRepository, memberIngredientRepository);

        Dish pork = dish(1L, "제육볶음", "돼지고기,양파,당근", "");
        Dish tofu = dish(2L, "마파두부", "두부,돼지고기,양파", "두반장");
        Dish seaweed = dish(3L, "미역국", "미역", "");
        when(dishRepository.findAllById(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return List.of(pork, tofu, seaweed).stream().filter(dish -> ids.contains(dish.getId())).toList();
        });
        when(ingredientRepository.findByNameIn(any())).thenAnswer(invocation -> {
            Collection<String> names = invocation.getArgument(0);
            return List.of(ingredient(10L, "돼지고기", "육류"), ingredient(11L, "양파", "채소"), ingredient(12L, "당근", "채소"),
                    ingredient(13L, "두부", "두부/콩"), ingredient(14L, "미역", "해조류")).stream()
                    .filter(ingredient -> names.contains(ingredient.getName())).toList();
        });
        when(ingredientRepository.findAllById(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return List.of(ingredient(10L, "돼지고기", "육류"), ingredient(11L, "양파", "채소")).stream()
                    .filter(ingredient -> ids.contains(ingredient.getId())).toList();
        });
        when(memberIngredientRepository.findIngredientIdsByMemberId(1L)).thenReturn(List.of(11L));

    }

    private Dish dish(Long id, String name, String ingredients, String seasonings) {

        return Dish.builder().id(id).name(name).role(DishRole.MAIN).mainIngredients("").methods("").flags("")
                .ingredients(ingredients).seasonings(seasonings).calorie(BigDecimal.TEN).protein(BigDecimal.ONE)
                .fat(BigDecimal.ONE).carbohydrate(BigDecimal.ONE).build();

    }

    private Ingredient ingredient(Long id, String name, String category) {

        return Ingredient.builder().id(id).name(name).category(category).build();

    }

    private List<String> names(List<ShoppingItem> items) {

        return items.stream().map(ShoppingItem::name).toList();

    }

    @Test
    @DisplayName("같은 재료는 한 줄로 합치고 어느 요리에 쓰이는지 함께 알려 준다.")
    void mergesSameIngredient() {

        ShoppingListResponse response = service.calculate(null, new ShoppingListRequest(List.of(1L, 2L, 3L), null));

        ShoppingItem onion = response.toBuy().stream().filter(item -> item.name().equals("양파")).findFirst().orElseThrow();
        assertEquals(List.of("제육볶음", "마파두부"), onion.usedIn());
        assertEquals(1, response.toBuy().stream().filter(item -> item.name().equals("돼지고기")).count());

    }

    @Test
    @DisplayName("분류 순서대로 정렬하고 같은 분류 안에서는 이름순이다.")
    void sortsByCategoryThenName() {

        ShoppingListResponse response = service.calculate(null, new ShoppingListRequest(List.of(1L, 2L, 3L), null));

        assertEquals(List.of("당근", "양파", "돼지고기", "두부", "미역"), names(response.toBuy()));

    }

    @Test
    @DisplayName("회원은 내 재료를 가진 재료로 보고 요청에 담은 재료와 합친다.")
    void combinesOwnedIngredients() {

        ShoppingListResponse response = service.calculate(1L, new ShoppingListRequest(List.of(1L, 2L), List.of(10L)));

        assertEquals(List.of("양파", "돼지고기"), names(response.have()));
        assertEquals(List.of("당근", "두부"), names(response.toBuy()));

    }

    @Test
    @DisplayName("비회원은 요청에 담은 재료만 가진 재료로 본다.")
    void guestUsesRequestedOwned() {

        ShoppingListResponse response = service.calculate(null, new ShoppingListRequest(List.of(1L), List.of(11L)));

        assertEquals(List.of("양파"), names(response.have()));
        assertEquals(List.of("당근", "돼지고기"), names(response.toBuy()));

    }

    @Test
    @DisplayName("특수 양념은 사야 할 재료와 따로 양념 분류로 알려 준다.")
    void separatesSeasonings() {

        ShoppingListResponse response = service.calculate(null, new ShoppingListRequest(List.of(2L), null));

        assertEquals(List.of("두반장"), names(response.seasonings()));
        assertEquals("양념", response.seasonings().get(0).category());
        assertEquals(List.of("마파두부"), response.seasonings().get(0).usedIn());
        assertTrue(!names(response.toBuy()).contains("두반장"));

    }

    @Test
    @DisplayName("같은 요리 번호를 두 번 보내도 한 번만 센다.")
    void ignoresDuplicateDishIds() {

        ShoppingListResponse response = service.calculate(null, new ShoppingListRequest(List.of(1L, 1L), null));

        assertEquals(3, response.toBuy().size());
        assertEquals(List.of("제육볶음"), response.toBuy().get(0).usedIn());

    }

    @Test
    @DisplayName("없는 요리나 없는 재료 번호면 예외가 난다.")
    void throwsForUnknownIds() {

        BusinessException dish = assertThrows(BusinessException.class,
                () -> service.calculate(null, new ShoppingListRequest(List.of(1L, 99L), null)));
        BusinessException ingredient = assertThrows(BusinessException.class,
                () -> service.calculate(null, new ShoppingListRequest(List.of(1L), List.of(999L))));

        assertEquals(ErrorCode.DISH_NOT_FOUND, dish.getErrorCode());
        assertEquals(ErrorCode.INGREDIENT_NOT_FOUND, ingredient.getErrorCode());

    }

}
