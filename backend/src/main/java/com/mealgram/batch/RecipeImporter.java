package com.mealgram.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mealgram.ingredient.Ingredient;
import com.mealgram.ingredient.IngredientRepository;
import com.mealgram.recipe.Recipe;
import com.mealgram.recipe.RecipeRepository;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

// 레시피/재료 시드 적재 배치

@Slf4j
@Component
@ConditionalOnProperty(name = "recipe.import.enabled", havingValue = "true")
public class RecipeImporter implements ApplicationRunner {

    private static final String INGREDIENT_SEED = "seed/ingredients.json";
    private static final String RECIPE_SEED = "seed/recipes.json";
    private static final String INSERT_RECIPE_INGREDIENT = "insert into recipe_ingredient (recipe_id, ingredient_id) values (?, ?)";

    private static final TypeReference<Map<String, List<String>>> INGREDIENTS = new TypeReference<>() {};
    private static final TypeReference<List<RecipeSeed>> RECIPES = new TypeReference<>() {};

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public RecipeImporter(RecipeRepository recipeRepository,
                          IngredientRepository ingredientRepository,
                          JdbcTemplate jdbcTemplate,
                          JsonMapper jsonMapper) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {

        if (recipeRepository.count() > 0) {
            log.info("레시피가 이미 있어서 적재를 건너뜁니다.");
            return;
        }

        Map<String, Ingredient> ingredients = saveIngredients();
        List<RecipeSeed> seeds = read(RECIPE_SEED, RECIPES);
        List<Recipe> recipes = saveRecipes(seeds);
        int linkCount = saveRecipeIngredients(seeds, recipes, ingredients);

        log.info("적재 완료: 재료 {}개, 레시피 {}개, 레시피 재료 연결 {}개", ingredients.size(), recipes.size(), linkCount);

    }

    private Map<String, Ingredient> saveIngredients() throws IOException {

        Map<String, Ingredient> byName = new HashMap<>();
        for (Ingredient ingredient : ingredientRepository.findAll()) {
            byName.put(ingredient.getName(), ingredient);
        }

        List<Ingredient> newIngredients = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : read(INGREDIENT_SEED, INGREDIENTS).entrySet()) {
            for (String name : entry.getValue()) {
                if (!byName.containsKey(name)) {
                    Ingredient ingredient = Ingredient.builder().name(name).category(entry.getKey()).build();
                    byName.put(name, ingredient);
                    newIngredients.add(ingredient);
                }
            }
        }
        ingredientRepository.saveAll(newIngredients);

        return byName;

    }

    private List<Recipe> saveRecipes(List<RecipeSeed> seeds) {

        List<Recipe> recipes = seeds.stream().map(this::toRecipe).toList();

        return recipeRepository.saveAll(recipes);

    }

    private int saveRecipeIngredients(List<RecipeSeed> seeds,
                                      List<Recipe> recipes,
                                      Map<String, Ingredient> ingredients) {

        List<Object[]> links = new ArrayList<>();
        for (int i = 0; i < seeds.size(); i++) {
            for (String name : seeds.get(i).ingredients()) {
                Ingredient ingredient = ingredients.get(name);
                if (ingredient == null) {
                    throw new IllegalStateException("재료 시드에 없는 재료입니다. 레시피 " + seeds.get(i).name() + ", 재료 " + name);
                }
                links.add(new Object[] {recipes.get(i).getId(), ingredient.getId()});
            }
        }
        jdbcTemplate.batchUpdate(INSERT_RECIPE_INGREDIENT, links);

        return links.size();

    }

    private Recipe toRecipe(RecipeSeed seed) {

        return Recipe.builder()
                .name(seed.name())
                .category(seed.category())
                .cookingMethod(seed.cookingMethod())
                .calorie(seed.calorie())
                .carbohydrate(seed.carbohydrate())
                .protein(seed.protein())
                .fat(seed.fat())
                .sodium(seed.sodium())
                .cookingSteps(seed.cookingSteps())
                .imageUrl(seed.imageUrl())
                .ingredientRawText(seed.ingredientRawText())
                .build();

    }

    private <T> T read(String path, TypeReference<T> type) throws IOException {

        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return jsonMapper.readValue(in, type);
        }

    }

}
