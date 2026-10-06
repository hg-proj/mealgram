package com.mealgram.meal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mealgram.recipe.dto.RecipeCandidate;

// 식단안 구성 맞춤

public final class MealComposer {

    private static final List<String> STAPLE_CATEGORIES = List.of("밥", "일품");
    private static final List<String> STAPLE_FILL = List.of("밥");
    private static final List<String> SOUP = List.of("국&찌개");
    private static final List<String> SIDE = List.of("반찬");
    private static final int SIDE_COUNT = 3;

    private MealComposer() {

    }

    public static List<Long> compose(List<Long> pickedIds,
                                     List<RecipeCandidate> candidates,
                                     Set<Long> usedIds) {

        Map<Long, RecipeCandidate> byId = candidates.stream()
                .collect(Collectors.toMap(RecipeCandidate::id, Function.identity()));
        List<RecipeCandidate> composed = new ArrayList<>();
        Set<String> names = new HashSet<>();

        take(composed, names, pickedIds.stream().map(byId::get).toList(), STAPLE_CATEGORIES, 1);
        take(composed, names, pickedIds.stream().map(byId::get).toList(), SOUP, 1);
        take(composed, names, pickedIds.stream().map(byId::get).toList(), SIDE, SIDE_COUNT);

        fill(composed, names, candidates, usedIds, STAPLE_CATEGORIES, STAPLE_FILL, 1);
        fill(composed, names, candidates, usedIds, SOUP, SOUP, 1);
        fill(composed, names, candidates, usedIds, SIDE, SIDE, SIDE_COUNT);

        return composed.stream().map(RecipeCandidate::id).toList();

    }

    private static void take(List<RecipeCandidate> composed,
                             Set<String> names,
                             List<RecipeCandidate> picked,
                             List<String> categories,
                             int limit) {

        long count = 0;
        for (RecipeCandidate candidate : picked) {
            if (count == limit) {
                return;
            }
            if (candidate != null && categories.contains(candidate.category()) && names.add(candidate.name())) {
                composed.add(candidate);
                count++;
            }
        }

    }

    private static void fill(List<RecipeCandidate> composed,
                             Set<String> names,
                             List<RecipeCandidate> candidates,
                             Set<Long> usedIds,
                             List<String> countCategories,
                             List<String> fillCategories,
                             int total) {

        long needed = total - composed.stream().filter(candidate -> countCategories.contains(candidate.category())).count();

        List<RecipeCandidate> knownFirst = candidates.stream()
                .sorted(Comparator.comparing((RecipeCandidate candidate) -> candidate.calorie() == null))
                .toList();

        for (int round = 0; round < 2 && needed > 0; round++) {
            boolean unusedOnly = round == 0;
            for (RecipeCandidate candidate : knownFirst) {
                if (needed == 0) {
                    break;
                }
                if (!fillCategories.contains(candidate.category()) || unusedOnly && usedIds.contains(candidate.id())
                        || !names.add(candidate.name())) {
                    continue;
                }
                composed.add(candidate);
                needed--;
            }
        }

    }

}
