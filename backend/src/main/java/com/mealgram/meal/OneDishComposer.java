package com.mealgram.meal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

import com.mealgram.dish.Dish;

// 일품식 한 끼 구성, 일품 하나에 종류별로 가벼운 곁들임만 붙임

final class OneDishComposer {

    private static final int ATTEMPTS = 400;
    private static final int CANDIDATE_SIZE = 8;
    private static final int TOP_SIZE = 3;
    private static final double PORTION_MIN = 0.8;
    private static final double PORTION_MAX = 1.25;
    private static final double OPTIONAL_RATE = 0.5;

    private OneDishComposer() {

    }

    static ComposedMeal compose(MealCondition condition,
                                Sides sides,
                                Map<Long, Integer> used,
                                RandomGenerator random,
                                boolean mustContainRequired) {

        if (sides.oneDish().isEmpty()) {
            return null;
        }

        List<ComposedMeal> candidates = new ArrayList<>();
        for (int attempt = 0; attempt < ATTEMPTS && candidates.size() < CANDIDATE_SIZE; attempt++) {
            Dish main = DishPicker.pick(sides.oneDish(), used, random);
            if (mustContainRequired && !main.matchesAny(condition.requiredNames())) {
                continue;
            }
            List<Dish> extras = extras(main, sides, used, random);
            double fixed = extras.stream().mapToDouble(dish -> dish.getCalorie().doubleValue()).sum();
            double portion = (condition.targetCalorie() - fixed) / main.getCalorie().doubleValue();
            if (portion < PORTION_MIN || portion > PORTION_MAX) {
                continue;
            }

            List<ComposedDish> dishes = new ArrayList<>();
            dishes.add(new ComposedDish(main, portion));
            extras.forEach(extra -> dishes.add(new ComposedDish(extra, 1)));
            ComposedMeal meal = new ComposedMeal(dishes, condition.requiredNames().isEmpty()
                    || dishes.stream().anyMatch(composed -> composed.dish().matchesAny(condition.requiredNames())));
            if (MealNutrition.summarize(meal, condition.targetCalorie(), condition.style()).passed()) {
                candidates.add(meal);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        candidates.sort(Comparator.comparingDouble(meal -> Math.abs(meal.dishes().get(0).portion() - 1)));

        return candidates.get(random.nextInt(Math.min(TOP_SIZE, candidates.size())));

    }

    private static List<Dish> extras(Dish main, Sides sides, Map<Long, Integer> used, RandomGenerator random) {

        List<Dish> extras = new ArrayList<>();
        for (String name : main.pairingList()) {
            Dish paired = sides.byName().get(name);
            if (paired != null) {
                extras.add(paired);
            }
        }
        if (!sides.dessert().isEmpty() && random.nextDouble() < OPTIONAL_RATE) {
            extras.add(DishPicker.pick(sides.dessert(), used, random));
        }

        return extras;

    }

    record Sides(List<Dish> oneDish, Map<String, Dish> byName, List<Dish> dessert) {

    }

}
