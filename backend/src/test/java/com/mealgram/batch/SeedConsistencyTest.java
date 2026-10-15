package com.mealgram.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class SeedConsistencyTest {

    private static Map<String, List<String>> ingredientsByCategory;
    private static List<RecipeSeed> recipes;

    @BeforeAll
    static void load() throws IOException {

        JsonMapper mapper = JsonMapper.builder().build();
        try (InputStream in = new ClassPathResource("seed/ingredients.json").getInputStream()) {
            ingredientsByCategory = mapper.readValue(in, new TypeReference<Map<String, List<String>>>() {});
        }
        try (InputStream in = new ClassPathResource("seed/recipes.json").getInputStream()) {
            recipes = mapper.readValue(in, new TypeReference<List<RecipeSeed>>() {});
        }

    }

    private Set<String> seedNames() {

        return ingredientsByCategory.values().stream().flatMap(List::stream).collect(Collectors.toSet());

    }

    private Set<String> usedNames() {

        return recipes.stream().flatMap(recipe -> recipe.ingredients().stream()).collect(Collectors.toSet());

    }

    @Test
    @DisplayName("재료 시드에 같은 이름이 두 번 들어 있지 않다.")
    void ingredientNamesAreUnique() {

        List<String> all = ingredientsByCategory.values().stream().flatMap(List::stream).toList();

        assertEquals(all.size(), new HashSet<>(all).size());

    }

    @Test
    @DisplayName("재료 시드의 모든 재료는 어떤 레시피에든 쓰인다.")
    void everyIngredientIsUsedByARecipe() {

        Set<String> unused = new HashSet<>(seedNames());
        unused.removeAll(usedNames());

        assertTrue(unused.isEmpty(), "쓰이지 않는 재료: " + unused);

    }

    @Test
    @DisplayName("레시피가 쓰는 모든 재료는 재료 시드에 있다.")
    void everyRecipeIngredientIsInSeed() {

        Set<String> missing = new HashSet<>(usedNames());
        missing.removeAll(seedNames());

        assertTrue(missing.isEmpty(), "시드에 없는 재료: " + missing);

    }

    @Test
    @DisplayName("레시피마다 재료가 하나 이상 있고 한 레시피에 같은 재료가 두 번 나오지 않는다.")
    void recipeIngredientsAreValid() {

        for (RecipeSeed recipe : recipes) {
            assertTrue(!recipe.ingredients().isEmpty(), recipe.name() + "에 재료가 없음");
            assertEquals(recipe.ingredients().size(), new HashSet<>(recipe.ingredients()).size(),
                    recipe.name() + "에 같은 재료가 두 번 있음");
        }

    }

}
