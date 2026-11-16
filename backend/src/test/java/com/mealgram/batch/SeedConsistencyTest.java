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

// 재료와 요리 시드 점검

class SeedConsistencyTest {

    private static final Set<String> ROLES = Set.of("RICE", "SOUP", "MAIN", "SIDE", "PICKLE", "DESSERT", "ONE_DISH");

    private static Map<String, List<String>> ingredientsByCategory;
    private static List<DishSeed> dishes;

    @BeforeAll
    static void load() throws IOException {

        JsonMapper mapper = JsonMapper.builder().build();
        try (InputStream in = new ClassPathResource("seed/ingredients.json").getInputStream()) {
            ingredientsByCategory = mapper.readValue(in, new TypeReference<Map<String, List<String>>>() {});
        }
        try (InputStream in = new ClassPathResource("seed/dishes.json").getInputStream()) {
            dishes = mapper.readValue(in, new TypeReference<List<DishSeed>>() {});
        }

    }

    @Test
    @DisplayName("재료 시드에 같은 이름이 두 번 들어 있지 않다.")
    void ingredientNamesAreUnique() {

        List<String> all = ingredientsByCategory.values().stream().flatMap(List::stream).toList();

        assertEquals(all.size(), new HashSet<>(all).size());

    }

    @Test
    @DisplayName("요리는 같은 역할 안에서 이름이 겹치지 않고 역할 값이 올바르다.")
    void dishesAreUniquePerRole() {

        Set<String> keys = new HashSet<>();
        for (DishSeed dish : dishes) {
            assertTrue(ROLES.contains(dish.role()), dish.name() + "의 역할이 올바르지 않음: " + dish.role());
            assertTrue(keys.add(dish.role() + ":" + dish.name()), dish.name() + "이 두 번 있음");
        }

    }

    @Test
    @DisplayName("요리마다 분량과 영양 값이 양수이고 역할별 개수가 충분하다.")
    void dishesHaveNutritionAndEnoughPerRole() {

        for (DishSeed dish : dishes) {
            assertTrue(dish.servingGram() > 0, dish.name() + "의 분량이 올바르지 않음");
            assertTrue(dish.calorie().signum() > 0, dish.name() + "의 열량이 올바르지 않음");
            assertTrue(dish.protein().signum() >= 0 && dish.fat().signum() >= 0 && dish.carbohydrate().signum() >= 0,
                    dish.name() + "의 영양 값이 올바르지 않음");
        }
        Map<String, Long> counts = dishes.stream().collect(Collectors.groupingBy(DishSeed::role, Collectors.counting()));
        for (String role : ROLES) {
            long minimum = "ONE_DISH".equals(role) ? 20 : 50;
            assertTrue(counts.getOrDefault(role, 0L) >= minimum, role + " 요리가 너무 적음: " + counts.get(role));
        }

    }

    @Test
    @DisplayName("단어 순서나 계란과 달걀 같은 표기만 다른 같은 요리가 두 번 들어 있지 않다.")
    void noNearDuplicateDishes() {

        Map<String, String> seen = new java.util.HashMap<>();
        for (DishSeed dish : dishes) {
            String key = dish.role() + ":" + normalized(dish.name());
            String other = seen.put(key, dish.name());
            assertTrue(other == null, other + "과 " + dish.name() + "은 같은 요리");
        }

    }

    private static String normalized(String name) {

        String value = name.replaceAll("[^가-힣A-Za-z0-9]", "");
        String[][] replacements = {{"계란", "달걀"}, {"쇠고기", "소고기"}, {"돈육", "돼지고기"}, {"우삼겹", "소고기"},
                {"애호박", "호박"}, {"새우살", "새우"}, {"돼지불고기", "돼지고기불고기"}, {"무우", "무"}, {"쌀보리", "보리쌀"}};
        for (String[] replacement : replacements) {
            value = value.replace(replacement[0], replacement[1]);
        }
        for (String modifier : List.of("순살", "매콤", "양념", "얼큰", "칼칼", "달콤", "새콤", "고소", "담백", "바삭", "촉촉", "찰",
                "흰", "햇", "생", "냉동", "한우", "국내산")) {
            value = value.replace(modifier, "");
        }
        char[] chars = value.toCharArray();
        java.util.Arrays.sort(chars);

        return new String(chars);

    }

    @Test
    @DisplayName("요리마다 재료가 있고 재료 이름은 재료 시드에 있거나 허용한 양념 채소다.")
    void dishIngredientsAreKnown() {

        Set<String> known = ingredientsByCategory.entrySet().stream()
                .filter(entry -> !"양념".equals(entry.getKey()) && !"도구".equals(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream()).collect(Collectors.toSet());
        known.addAll(Set.of("대파", "쪽파", "청고추", "홍고추"));
        for (DishSeed dish : dishes) {
            assertTrue(dish.ingredients() != null, dish.name() + "에 재료 목록이 없음");
            if (!"DESSERT".equals(dish.role())) {
                assertTrue(!dish.ingredients().isEmpty(), dish.name() + "의 재료가 비어 있음");
            }
            for (String ingredient : dish.ingredients()) {
                assertTrue(known.contains(ingredient), dish.name() + "의 재료 " + ingredient + "이 재료 시드에 없음");
            }
            assertEquals(dish.ingredients().size(), new HashSet<>(dish.ingredients()).size(), dish.name() + "에 같은 재료가 두 번 있음");
        }

    }

    @Test
    @DisplayName("특수 양념은 양념 분류의 이름이고 재료 이름과 겹치지 않는다.")
    void seasoningsAreKnown() {

        Set<String> seasonings = new HashSet<>(ingredientsByCategory.get("양념"));
        Set<String> ingredients = ingredientsByCategory.entrySet().stream()
                .filter(entry -> !"양념".equals(entry.getKey()) && !"도구".equals(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream()).collect(Collectors.toSet());
        for (DishSeed dish : dishes) {
            if (dish.seasonings() != null) {
                for (String seasoning : dish.seasonings()) {
                    assertTrue(seasonings.contains(seasoning), dish.name() + "의 양념 " + seasoning + "이 양념 시드에 없음");
                    assertTrue(!ingredients.contains(seasoning), dish.name() + "의 양념 " + seasoning + "이 재료 이름과 겹침");
                }
            }
        }

    }

    @Test
    @DisplayName("일품에 정해 둔 곁들임은 모두 시드에 있는 요리다.")
    void pairingsExist() {

        Set<String> names = dishes.stream().filter(dish -> !"ONE_DISH".equals(dish.role()) && !"MAIN".equals(dish.role()))
                .map(DishSeed::name).collect(Collectors.toSet());
        for (DishSeed dish : dishes) {
            if (dish.pairings() != null) {
                for (String paired : dish.pairings()) {
                    assertTrue(names.contains(paired), dish.name() + "의 곁들임 " + paired + "이 시드에 없음");
                }
            }
        }

    }

    @Test
    @DisplayName("메인 반찬과 일품에는 사진이 있고 사진 파일도 있다.")
    void mainAndOneDishHavePhotos() {

        for (DishSeed dish : dishes) {
            if ("MAIN".equals(dish.role()) || "ONE_DISH".equals(dish.role())) {
                assertTrue(dish.imageGroup() != null, dish.name() + "에 사진이 없음");
                assertTrue(new ClassPathResource("static/images/dishes/" + dish.imageGroup() + ".webp").exists(),
                        dish.name() + "의 사진 파일이 없음: " + dish.imageGroup());
            }
        }

    }

    @Test
    @DisplayName("국에는 국물이 없는 요리가 들어 있지 않다.")
    void soupsAreLiquid() {

        Set<String> notSoup = Set.of("맛탕");
        for (DishSeed dish : dishes) {
            if ("SOUP".equals(dish.role())) {
                assertTrue(notSoup.stream().noneMatch(dish.name()::contains), dish.name() + "은 국이 아님");
            }
        }

    }

}
