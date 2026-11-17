package com.mealgram.meal;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mealgram.dish.Dish;
import com.mealgram.dish.DishRole;

// 규칙으로 구성한 식단 한 개

public record ComposedMeal(List<ComposedDish> dishes, boolean requiredMet) {

    public Set<String> mainFamilies() {

        return dishes.stream()
                .map(ComposedDish::dish)
                .filter(dish -> dish.getRole() == DishRole.MAIN || dish.getRole() == DishRole.ONE_DISH)
                .findFirst()
                .map(Dish::families)
                .orElse(Set.of());

    }

    public Set<String> photoGroups() {

        return dishes.stream()
                .map(dish -> dish.dish().getImageGroup())
                .filter(group -> group != null && !group.isBlank())
                .collect(Collectors.toSet());

    }

    public double calorie() {

        return dishes.stream().mapToDouble(ComposedDish::calorie).sum();

    }

    public double protein() {

        return dishes.stream().mapToDouble(ComposedDish::protein).sum();

    }

    public double fat() {

        return dishes.stream().mapToDouble(ComposedDish::fat).sum();

    }

    public double carbohydrate() {

        return dishes.stream().mapToDouble(ComposedDish::carbohydrate).sum();

    }

}
