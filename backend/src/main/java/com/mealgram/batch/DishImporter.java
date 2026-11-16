package com.mealgram.batch;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mealgram.dish.Dish;
import com.mealgram.dish.DishRepository;
import com.mealgram.dish.DishRole;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

// 요리 시드 적재 배치

@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "dish.import.enabled", havingValue = "true")
public class DishImporter implements ApplicationRunner {

    private static final String DISH_SEED = "seed/dishes.json";
    private static final TypeReference<List<DishSeed>> DISHES = new TypeReference<>() {};

    private final DishRepository dishRepository;
    private final JsonMapper jsonMapper;

    public DishImporter(DishRepository dishRepository, JsonMapper jsonMapper) {
        this.dishRepository = dishRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {

        if (dishRepository.count() > 0) {
            log.info("요리가 이미 있어서 적재를 건너뜁니다.");
            return;
        }

        List<Dish> dishes = read().stream().map(this::toDish).toList();
        dishRepository.saveAll(dishes);

        log.info("적재 완료: 요리 {}개", dishes.size());

    }

    private List<DishSeed> read() throws IOException {

        try (InputStream in = new ClassPathResource(DISH_SEED).getInputStream()) {
            return jsonMapper.readValue(in, DISHES);
        }

    }

    private Dish toDish(DishSeed seed) {

        return Dish.builder()
                .name(seed.name())
                .role(DishRole.valueOf(seed.role()))
                .cuisine(seed.cuisine())
                .mainIngredients(String.join(Dish.LIST_SEPARATOR, seed.mainIngredients()))
                .methods(String.join(Dish.LIST_SEPARATOR, seed.methods()))
                .flags(String.join(Dish.LIST_SEPARATOR, seed.flags()))
                .dessertType(seed.dessertType())
                .imageGroup(seed.imageGroup())
                .heavySoup(seed.heavySoup())
                .servingGram(seed.servingGram())
                .calorie(seed.calorie())
                .protein(seed.protein())
                .fat(seed.fat())
                .carbohydrate(seed.carbohydrate())
                .source(seed.source())
                .frequency(seed.frequency())
                .pairings(seed.pairings() == null ? "" : String.join(Dish.LIST_SEPARATOR, seed.pairings()))
                .ingredients(seed.ingredients() == null ? "" : String.join(Dish.LIST_SEPARATOR, seed.ingredients()))
                .seasonings(seed.seasonings() == null ? "" : String.join(Dish.LIST_SEPARATOR, seed.seasonings()))
                .build();

    }

}
