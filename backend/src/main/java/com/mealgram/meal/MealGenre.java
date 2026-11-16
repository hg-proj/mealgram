package com.mealgram.meal;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// 식단의 장르

final class MealGenre {

    private static final String KOREAN = "한식";

    private MealGenre() {
    }

    static String of(List<ComposedDish> dishes) {

        return of(dishes, List.of());

    }

    static String of(List<ComposedDish> dishes, List<String> chosen) {

        Map<String, Long> counts = dishes.stream()
                .map(composed -> composed.dish().getCuisine())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(cuisine -> cuisine, Collectors.counting()));

        String chosenOne = chosen.stream()
                .filter(counts::containsKey)
                .max(Comparator.comparing((String genre) -> counts.get(genre))
                        .thenComparing(genre -> !KOREAN.equals(genre)))
                .orElse(null);
        if (chosenOne != null) {
            return chosenOne;
        }

        return counts.entrySet().stream()
                .max(Comparator.comparing((Map.Entry<String, Long> entry) -> entry.getValue())
                        .thenComparing(entry -> KOREAN.equals(entry.getKey()))
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElse(null);

    }

}
