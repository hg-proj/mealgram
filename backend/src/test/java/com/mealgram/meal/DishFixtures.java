package com.mealgram.meal;

import java.math.BigDecimal;
import java.util.List;

import com.mealgram.dish.Dish;
import com.mealgram.dish.DishRole;

// 테스트용 요리 만들기

final class DishFixtures {

    private static long nextId = 1;

    private DishFixtures() {

    }

    static Dish dish(String name, DishRole role, double calorie, double protein, double fat, double carbohydrate) {

        return dish(name, role, "한식", calorie, protein, fat, carbohydrate, List.of(), List.of(), List.of());

    }

    static Dish dish(String name, DishRole role, String cuisine, double calorie, double protein, double fat,
                     double carbohydrate, List<String> ingredients, List<String> methods, List<String> flags) {

        return Dish.builder()
                .id(nextId++)
                .name(name)
                .role(role)
                .cuisine(cuisine)
                .mainIngredients(String.join(Dish.LIST_SEPARATOR, ingredients))
                .ingredients(String.join(Dish.LIST_SEPARATOR, ingredients))
                .methods(String.join(Dish.LIST_SEPARATOR, methods))
                .flags(String.join(Dish.LIST_SEPARATOR, flags))
                .dessertType(role == DishRole.DESSERT ? Dish.FRUIT : null)
                .servingGram(100)
                .calorie(BigDecimal.valueOf(calorie))
                .protein(BigDecimal.valueOf(protein))
                .fat(BigDecimal.valueOf(fat))
                .carbohydrate(BigDecimal.valueOf(carbohydrate))
                .source("공식")
                .frequency(100)
                .build();

    }

    static Dish oneDish(String name, double calorie, String kind, String pairings) {

        return Dish.builder()
                .id(nextId++)
                .name(name)
                .role(DishRole.ONE_DISH)
                .cuisine("한식")
                .mainIngredients("")
                .methods("")
                .flags(kind)
                .servingGram(400)
                .calorie(BigDecimal.valueOf(calorie))
                .protein(BigDecimal.valueOf(calorie * 0.15 / 4))
                .fat(BigDecimal.valueOf(calorie * 0.25 / 9))
                .carbohydrate(BigDecimal.valueOf(calorie * 0.6 / 4))
                .source("공식")
                .frequency(100)
                .pairings(pairings)
                .build();

    }

    static Dish mainWithPhoto(String name, String imageGroup) {

        return Dish.builder()
                .id(nextId++)
                .name(name)
                .role(DishRole.MAIN)
                .cuisine("한식")
                .mainIngredients("")
                .ingredients("")
                .methods("")
                .flags("")
                .imageGroup(imageGroup)
                .servingGram(100)
                .calorie(BigDecimal.valueOf(250))
                .protein(BigDecimal.valueOf(20))
                .fat(BigDecimal.valueOf(12))
                .carbohydrate(BigDecimal.valueOf(15))
                .source("공식")
                .frequency(100)
                .build();

    }

    static Dish mainWithPhoto(String name, String imageGroup, List<String> ingredients) {

        return Dish.builder()
                .id(nextId++)
                .name(name)
                .role(DishRole.MAIN)
                .cuisine("한식")
                .mainIngredients(String.join(Dish.LIST_SEPARATOR, ingredients))
                .ingredients(String.join(Dish.LIST_SEPARATOR, ingredients))
                .methods("")
                .flags("")
                .imageGroup(imageGroup)
                .servingGram(100)
                .calorie(BigDecimal.valueOf(150))
                .protein(BigDecimal.valueOf(18))
                .fat(BigDecimal.valueOf(6))
                .carbohydrate(BigDecimal.valueOf(6))
                .source("공식")
                .frequency(100)
                .build();

    }

    static ComposedMeal meal(boolean requiredMet, Dish... dishes) {

        return new ComposedMeal(java.util.Arrays.stream(dishes).map(dish -> new ComposedDish(dish, 1)).toList(), requiredMet);

    }

}
