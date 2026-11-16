package com.mealgram.meal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

import com.mealgram.dish.Dish;

// 요리 가중 선택, 자주 나온 요리를 더 자주 고르고 이미 쓴 요리는 덜 고름

final class DishPicker {

    private static final double FREQUENCY_POWER = 0.25;
    private static final double USED_PENALTY = 2;
    private static final int MAX_USED_ROUNDS = 3;

    private DishPicker() {

    }

    static Dish pick(List<Dish> dishes, Map<Long, Integer> used, RandomGenerator random) {

        double[] weights = dishes.stream()
                .mapToDouble(dish -> Math.pow(dish.getFrequency(), FREQUENCY_POWER)
                        / (1 + USED_PENALTY * Math.min(used.getOrDefault(dish.getId(), 0), MAX_USED_ROUNDS)))
                .toArray();
        double point = random.nextDouble() * Arrays.stream(weights).sum();
        for (int i = 0; i < weights.length; i++) {
            point -= weights[i];
            if (point <= 0) {
                return dishes.get(i);
            }
        }

        return dishes.get(dishes.size() - 1);

    }

}
