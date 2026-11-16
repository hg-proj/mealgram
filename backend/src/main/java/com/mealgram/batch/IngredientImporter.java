package com.mealgram.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mealgram.ingredient.Ingredient;
import com.mealgram.ingredient.IngredientRepository;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

// 재료 시드 적재 배치, 이미 있는 재료는 두고 없는 재료만 넣음

@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "ingredient.import.enabled", havingValue = "true")
public class IngredientImporter implements ApplicationRunner {

    private static final String INGREDIENT_SEED = "seed/ingredients.json";
    private static final TypeReference<Map<String, List<String>>> INGREDIENTS = new TypeReference<>() {};

    private final IngredientRepository ingredientRepository;
    private final JsonMapper jsonMapper;

    public IngredientImporter(IngredientRepository ingredientRepository, JsonMapper jsonMapper) {
        this.ingredientRepository = ingredientRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {

        Set<String> existing = ingredientRepository.findAll().stream().map(Ingredient::getName).collect(Collectors.toSet());
        List<Ingredient> missing = read().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .filter(name -> !existing.contains(name))
                        .map(name -> Ingredient.builder().name(name).category(entry.getKey()).build()))
                .toList();
        if (missing.isEmpty()) {
            log.info("새로 적재할 재료가 없습니다.");
            return;
        }
        ingredientRepository.saveAll(missing);

        log.info("적재 완료: 재료 {}개", missing.size());

    }

    private Map<String, List<String>> read() throws IOException {

        try (InputStream in = new ClassPathResource(INGREDIENT_SEED).getInputStream()) {
            return jsonMapper.readValue(in, INGREDIENTS);
        }

    }

}
