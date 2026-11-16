package com.mealgram.meal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.mealgram.dish.Dish;
import com.mealgram.dish.DishRepository;
import com.mealgram.dish.DishRole;

// 요리 조합 규칙으로 식단 구성

@Component
public class DishComposer {

    private static final String DIET = "다이어트";
    private static final String BULK = "벌크업";
    private static final String KOREAN = "한식";
    private static final String CHINESE = "중식";
    private static final Set<String> LIMITED_METHODS = Set.of("볶음", "조림", "구이", "찜", "전부침", "튀김");
    private static final String PAN_FRIED = "전부침";

    private static final int ATTEMPTS = 800;
    private static final int STYLE_ATTEMPTS = 1200;
    private static final int CANDIDATE_SIZE = 25;
    private static final int TOP_SIZE = 6;
    private static final int SIDE_COUNT = 2;
    private static final double KIMCHI_RATE = 0.8;
    private static final double DESSERT_RATE = 0.6;

    private static final double CALORIE_TOLERANCE = 0.05;
    private static final double MAIN_MIN = 0.7;
    private static final double MAIN_MAX = 1.3;
    private static final double MAIN_MAX_FOR_STYLE = 1.5;
    private static final double RICE_MIN = 0.8;
    private static final double RICE_MIN_LOW_CARB = 0.5;
    private static final double RICE_MAX = 1.2;
    private static final double RICE_MAX_BULK = 1.4;
    private static final double MAIN_EXTRA_BULK = 0.3;
    private static final double RICE_DIET = 0.9;
    private static final double RICE_BULK = 1.1;
    private static final double RICE_KEEP = 1.0;
    private static final double DIET_MAIN_MAX_CALORIE = 190;
    private static final double DIET_PROTEIN_MIN_GRAM = 22;
    private static final double BULK_PROTEIN_MIN_GRAM = 35;
    private static final double KEEP_PROTEIN_MIN_GRAM = 25;
    private static final double DIET_FAT_MAX_PERCENT = 30;
    private static final double HIGH_PROTEIN_MIN_PERCENT = 25;
    private static final double LOW_CARB_MAX_PERCENT = 45;
    private static final double LOW_FAT_MAX_PERCENT = 20;
    private static final double HIGH_PROTEIN_MAIN_DENSITY = 0.3;
    private static final double LOW_CARB_MAIN_DENSITY = 0.35;
    private static final double LOW_CARB_SIDE_DENSITY = 0.6;
    private static final double CARBOHYDRATE_KCAL_PER_GRAM = 4;
    private static final double PROTEIN_KCAL_PER_GRAM = 4;
    private static final double FAT_KCAL_PER_GRAM = 9;
    private static final double PREFERRED_BONUS = 0.1;
    private static final double PREFERRED_BONUS_MAX = 0.3;
    private static final double GENRE_RATE = 0.7;
    private static final double GENRE_BONUS = 0.15;
    private static final double GENRE_BONUS_MAX = 0.45;
    private static final int PHOTO_RETRY = 8;
    private static final int ONE_DISH_MIN_COUNT = 3;
    private static final int ONE_DISH_SHARE = 4;
    private static final int ONE_DISH_FIRST_INDEX = 2;
    private static final int ONE_DISH_GAP = 3;

    private final DishRepository dishRepository;

    private volatile Map<DishRole, List<Dish>> pool;

    public DishComposer(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    public List<ComposedMeal> compose(MealCondition condition, int count, RandomGenerator random) {

        Pools pools = pools(condition);
        Map<Long, Integer> used = new HashMap<>();
        boolean needRequired = !condition.requiredNames().isEmpty();

        List<ComposedMeal> oneDishMeals = new ArrayList<>();
        int oneDishCount = pools.oneDish().isEmpty() || count < ONE_DISH_MIN_COUNT ? 0 : Math.max(1, count / ONE_DISH_SHARE);
        OneDishComposer.Sides sides = new OneDishComposer.Sides(pools.oneDish(), pairable(), pools.dessert());
        for (int i = 0; i < oneDishCount; i++) {
            ComposedMeal meal = OneDishComposer.compose(condition, sides, used, random, needRequired);
            if (meal == null) {
                break;
            }
            oneDishMeals.add(meal);
            meal.dishes().forEach(composed -> used.merge(composed.dish().getId(), 1, Integer::sum));
        }

        List<ComposedMeal> meals = new ArrayList<>();
        Set<String> usedPhotos = new HashSet<>();
        oneDishMeals.forEach(meal -> usedPhotos.addAll(meal.photoGroups()));
        for (int i = 0; i < count - oneDishMeals.size(); i++) {
            ComposedMeal meal = composeOne(condition, pools, used, random, needRequired);
            if (meal == null && needRequired) {
                meal = composeOne(condition, pools, used, random, false);
            }
            if (meal != null && needRequired) {
                meal = distinctPhoto(meal, condition, pools, used, random, usedPhotos);
            }
            if (meal == null) {
                break;
            }
            usedPhotos.addAll(meal.photoGroups());
            meals.add(meal);
            meal.dishes().forEach(composed -> used.merge(composed.dish().getId(), 1, Integer::sum));
        }
        for (int i = 0; i < oneDishMeals.size(); i++) {
            meals.add(Math.min(ONE_DISH_FIRST_INDEX + i * ONE_DISH_GAP, meals.size()), oneDishMeals.get(i));
        }

        return meals;

    }

    private ComposedMeal distinctPhoto(ComposedMeal meal,
                                       MealCondition condition,
                                       Pools pools,
                                       Map<Long, Integer> used,
                                       RandomGenerator random,
                                       Set<String> usedPhotos) {

        if (meal.photoGroups().stream().noneMatch(usedPhotos::contains)) {
            return meal;
        }

        for (int attempt = 0; attempt < PHOTO_RETRY; attempt++) {
            ComposedMeal other = composeOne(condition, pools, used, random, false);
            if (other != null && other.photoGroups().stream().noneMatch(usedPhotos::contains)) {
                return other;
            }
        }

        return meal;

    }

    private ComposedMeal composeOne(MealCondition condition,
                                    Pools pools,
                                    Map<Long, Integer> used,
                                    RandomGenerator random,
                                    boolean mustContainRequired) {

        if (pools.rice().isEmpty() || pools.soup().isEmpty() || pools.main().isEmpty() || pools.side().isEmpty()) {
            return null;
        }

        DietStyle style = condition.style();
        boolean diet = DIET.equals(condition.goal());
        boolean bulk = BULK.equals(condition.goal());
        double riceBase = diet ? RICE_DIET : bulk ? RICE_BULK : RICE_KEEP;
        double mainMax = (style == DietStyle.LOW_CARB || style == DietStyle.HIGH_PROTEIN ? MAIN_MAX_FOR_STYLE : MAIN_MAX)
                + (bulk ? MAIN_EXTRA_BULK : 0);
        double riceMin = style == DietStyle.LOW_CARB ? RICE_MIN_LOW_CARB : RICE_MIN;
        int attempts = style == DietStyle.BALANCED || style == DietStyle.LOW_FAT ? ATTEMPTS : STYLE_ATTEMPTS;
        List<Dish> genreSides = pools.side().stream().filter(dish -> isGenre(dish, condition.genres())).toList();
        List<Candidate> candidates = new ArrayList<>();
        for (int attempt = 0; attempt < attempts && candidates.size() < CANDIDATE_SIZE; attempt++) {
            Dish rice = pick(pools.rice(), used, random);
            Dish soup = pick(pools.soup(), used, random);
            Dish main = pick(pools.main(), used, random);
            List<Dish> sides = pickSides(pools.side(), used, random);
            if (sides.size() < SIDE_COUNT) {
                continue;
            }
            if (!genreSides.isEmpty() && random.nextDouble() < GENRE_RATE) {
                Dish genreSide = pick(genreSides, used, random);
                if (!sides.contains(genreSide)) {
                    sides = List.of(genreSide, sides.get(0));
                }
            }
            Dish pickle = random.nextDouble() < KIMCHI_RATE ? pick(pools.pickle(), used, random) : null;
            Dish dessert = random.nextDouble() < DESSERT_RATE && !pools.dessert().isEmpty()
                    ? pick(pools.dessert(), used, random) : null;
            if (mustContainRequired) {
                Forced forced = force(pools, condition.requiredNames(), used, random);
                if (forced == null) {
                    return null;
                }
                rice = forced.rice() != null ? forced.rice() : rice;
                soup = forced.soup() != null ? forced.soup() : soup;
                main = forced.main() != null ? forced.main() : main;
                if (forced.side() != null && !sides.contains(forced.side())) {
                    sides = List.of(forced.side(), sides.get(0));
                }
                pickle = forced.pickle() != null ? forced.pickle() : pickle;
                dessert = forced.dessert() != null ? forced.dessert() : dessert;
            }
            Candidate candidate = build(condition, rice, soup, main, sides, pickle, dessert, riceBase, riceMin, mainMax);
            if (candidate != null && (!mustContainRequired || candidate.requiredMet())) {
                candidates.add(candidate);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }

        candidates.sort(Comparator.comparingDouble(Candidate::penalty));
        Candidate chosen = candidates.get(random.nextInt(Math.min(TOP_SIZE, candidates.size())));

        return new ComposedMeal(chosen.dishes(), chosen.requiredMet());

    }

    private Candidate build(MealCondition condition, Dish rice, Dish soup, Dish main, List<Dish> sides, Dish pickle,
                            Dish dessert, double riceBase, double riceMin, double mainMax) {

        DietStyle style = condition.style();
        boolean diet = DIET.equals(condition.goal());
        boolean bulk = BULK.equals(condition.goal());
        List<Dish> dishes = Stream.concat(Stream.of(soup, main), sides.stream()).toList();
        List<Dish> withPickle = pickle == null ? dishes : Stream.concat(dishes.stream(), Stream.of(pickle)).toList();

        List<String> ingredients = dishes.stream().flatMap(dish -> dish.mainIngredientList().stream()).toList();
        long ingredientDishes = dishes.stream().filter(dish -> !dish.mainIngredientList().isEmpty()).count();
        if (ingredients.size() != ingredients.stream().distinct().count() || ingredientDishes > (bulk ? 3 : 2)) {
            return null;
        }
        if (withPickle.stream().filter(dish -> dish.hasFlag(Dish.SPICY)).count() > 1
                || withPickle.stream().filter(Dish::isPickled).count() > 2
                || dishes.stream().filter(dish -> dish.hasFlag(Dish.FRIED)).count() > 1) {
            return null;
        }
        Map<String, Long> methods = Stream.concat(Stream.of(main), sides.stream())
                .flatMap(dish -> dish.methodList().stream())
                .filter(LIMITED_METHODS::contains)
                .collect(Collectors.groupingBy(method -> method, Collectors.counting()));
        if (methods.values().stream().anyMatch(times -> times > 2) || methods.getOrDefault(PAN_FRIED, 0L) > 1) {
            return null;
        }

        double fixed = soup.getCalorie().doubleValue() + sides.stream().mapToDouble(dish -> dish.getCalorie().doubleValue()).sum()
                + (pickle == null ? 0 : pickle.getCalorie().doubleValue())
                + (dessert == null ? 0 : dessert.getCalorie().doubleValue());
        double riceCalorie = rice.getCalorie().doubleValue();
        double mainCalorie = main.getCalorie().doubleValue();
        int target = condition.targetCalorie();
        double mainPortion = (target - fixed - riceBase * riceCalorie) / mainCalorie;
        double ricePortion = riceBase;
        if (mainPortion < MAIN_MIN) {
            mainPortion = MAIN_MIN;
            ricePortion = (target - fixed - mainPortion * mainCalorie) / riceCalorie;
        } else if (mainPortion > mainMax) {
            mainPortion = mainMax;
            ricePortion = (target - fixed - mainPortion * mainCalorie) / riceCalorie;
        }
        if (ricePortion < riceMin || ricePortion > (bulk ? RICE_MAX_BULK : RICE_MAX)) {
            return null;
        }

        List<ComposedDish> composed = new ArrayList<>();
        composed.add(new ComposedDish(rice, ricePortion));
        composed.add(new ComposedDish(soup, 1));
        composed.add(new ComposedDish(main, mainPortion));
        sides.forEach(side -> composed.add(new ComposedDish(side, 1)));
        if (pickle != null) {
            composed.add(new ComposedDish(pickle, 1));
        }
        if (dessert != null) {
            composed.add(new ComposedDish(dessert, 1));
        }
        ComposedMeal meal = new ComposedMeal(composed, matchesAny(composed, condition.requiredNames()));

        double calorie = meal.calorie();
        if (Math.abs(calorie - target) / target > CALORIE_TOLERANCE) {
            return null;
        }
        double energy = meal.carbohydrate() * CARBOHYDRATE_KCAL_PER_GRAM + meal.protein() * PROTEIN_KCAL_PER_GRAM
                + meal.fat() * FAT_KCAL_PER_GRAM;
        double proteinPercent = meal.protein() * PROTEIN_KCAL_PER_GRAM / energy * 100;
        double fatPercent = meal.fat() * FAT_KCAL_PER_GRAM / energy * 100;
        double carbohydratePercent = meal.carbohydrate() * CARBOHYDRATE_KCAL_PER_GRAM / energy * 100;
        double proteinMin = diet ? DIET_PROTEIN_MIN_GRAM : bulk ? BULK_PROTEIN_MIN_GRAM : KEEP_PROTEIN_MIN_GRAM;
        if (meal.protein() < proteinMin || diet && fatPercent > DIET_FAT_MAX_PERCENT) {
            return null;
        }
        if (style == DietStyle.HIGH_PROTEIN && proteinPercent < HIGH_PROTEIN_MIN_PERCENT
                || style == DietStyle.LOW_CARB && carbohydratePercent > LOW_CARB_MAX_PERCENT
                || style == DietStyle.LOW_FAT && fatPercent > LOW_FAT_MAX_PERCENT) {
            return null;
        }

        double penalty = Math.abs(mainPortion - 1) * 2 + Math.abs(ricePortion - riceBase)
                - Math.min(PREFERRED_BONUS_MAX, PREFERRED_BONUS * countMatches(composed, condition.preferredNames()))
                - Math.min(GENRE_BONUS_MAX, GENRE_BONUS * countGenres(composed, condition.genres()));

        return new Candidate(penalty, composed, meal.requiredMet());

    }

    private Forced force(Pools pools, List<String> requiredNames, Map<Long, Integer> used, RandomGenerator random) {

        List<Dish> matching = Stream.of(pools.rice(), pools.soup(), pools.main(), pools.side(), pools.pickle(), pools.dessert())
                .flatMap(List::stream)
                .filter(dish -> matches(dish, requiredNames))
                .toList();
        if (matching.isEmpty()) {
            return null;
        }
        Dish dish = pick(matching, used, random);

        return new Forced(
                dish.getRole() == DishRole.RICE ? dish : null,
                dish.getRole() == DishRole.SOUP ? dish : null,
                dish.getRole() == DishRole.MAIN ? dish : null,
                dish.getRole() == DishRole.SIDE ? dish : null,
                dish.getRole() == DishRole.PICKLE ? dish : null,
                dish.getRole() == DishRole.DESSERT ? dish : null);

    }

    private List<Dish> pickSides(List<Dish> sides, Map<Long, Integer> used, RandomGenerator random) {

        List<Dish> picked = new ArrayList<>();
        for (int i = 0; i < SIDE_COUNT && !sides.isEmpty(); i++) {
            Dish dish = pick(sides, used, random);
            if (!picked.contains(dish)) {
                picked.add(dish);
            }
        }

        return picked;

    }

    private Dish pick(List<Dish> dishes, Map<Long, Integer> used, RandomGenerator random) {

        return DishPicker.pick(dishes, used, random);

    }

    private boolean matches(Dish dish, List<String> names) {

        return dish.matchesAny(names);

    }

    private boolean matchesAny(List<ComposedDish> dishes, List<String> names) {

        return names.isEmpty() || dishes.stream().anyMatch(composed -> matches(composed.dish(), names));

    }

    private long countGenres(List<ComposedDish> dishes, List<String> genres) {

        return dishes.stream().filter(composed -> isGenre(composed.dish(), genres)).count();

    }

    private boolean isGenre(Dish dish, List<String> genres) {

        return !KOREAN.equals(dish.getCuisine()) && genres.contains(dish.getCuisine());

    }

    private boolean allowedCuisine(Dish dish, MealCondition condition, boolean withChinese) {

        String cuisine = dish.getCuisine();

        return KOREAN.equals(cuisine) || withChinese && CHINESE.equals(cuisine) || condition.genres().contains(cuisine);

    }

    private long countMatches(List<ComposedDish> dishes, List<String> names) {

        return dishes.stream().filter(composed -> matches(composed.dish(), names)).count();

    }

    private Pools pools(MealCondition condition) {

        Map<DishRole, List<Dish>> all = allDishes();
        DietStyle style = condition.style();
        boolean diet = DIET.equals(condition.goal());
        List<Dish> soup = all.getOrDefault(DishRole.SOUP, List.of()).stream()
                .filter(dish -> !dish.isStapleSoup())
                .filter(dish -> !(dish.isHeavySoup() && (diet || style == DietStyle.HIGH_PROTEIN)))
                .toList();
        List<Dish> main = all.getOrDefault(DishRole.MAIN, List.of()).stream()
                .filter(dish -> allowedCuisine(dish, condition, true))
                .filter(dish -> !(diet && (dish.hasFlag(Dish.FRIED) || dish.hasFlag(Dish.FATTY)
                        || dish.getCalorie().doubleValue() > DIET_MAIN_MAX_CALORIE)))
                .filter(dish -> style != DietStyle.HIGH_PROTEIN || density(dish, dish.getProtein().doubleValue()) >= HIGH_PROTEIN_MAIN_DENSITY)
                .filter(dish -> style != DietStyle.LOW_CARB || density(dish, dish.getCarbohydrate().doubleValue()) <= LOW_CARB_MAIN_DENSITY)
                .toList();
        List<Dish> side = all.getOrDefault(DishRole.SIDE, List.of()).stream()
                .filter(dish -> allowedCuisine(dish, condition, false) && !dish.hasFlag(Dish.FRIED))
                .filter(dish -> style != DietStyle.LOW_CARB || density(dish, dish.getCarbohydrate().doubleValue()) <= LOW_CARB_SIDE_DENSITY)
                .toList();
        List<Dish> dessert = all.getOrDefault(DishRole.DESSERT, List.of()).stream()
                .filter(dish -> Dish.FRUIT.equals(dish.getDessertType()) || !diet && Dish.DAIRY.equals(dish.getDessertType()))
                .toList();

        return new Pools(all.getOrDefault(DishRole.RICE, List.of()), soup, main, side,
                all.getOrDefault(DishRole.PICKLE, List.of()), dessert, all.getOrDefault(DishRole.ONE_DISH, List.of()));

    }

    private Map<String, Dish> pairable() {

        Map<String, Dish> byName = new HashMap<>();
        for (DishRole role : List.of(DishRole.PICKLE, DishRole.SOUP, DishRole.SIDE, DishRole.DESSERT)) {
            allDishes().getOrDefault(role, List.of()).forEach(dish -> byName.putIfAbsent(dish.getName(), dish));
        }

        return byName;

    }

    private double density(Dish dish, double nutrient) {

        double calorie = Math.max(dish.getCalorie().doubleValue(), 1);

        return nutrient * (nutrient == dish.getProtein().doubleValue() ? PROTEIN_KCAL_PER_GRAM : CARBOHYDRATE_KCAL_PER_GRAM) / calorie;

    }

    private Map<DishRole, List<Dish>> allDishes() {

        Map<DishRole, List<Dish>> loaded = pool;
        if (loaded == null) {
            loaded = dishRepository.findAll().stream().collect(Collectors.groupingBy(Dish::getRole));
            if (!loaded.isEmpty()) {
                pool = loaded;
            }
        }

        return loaded;

    }

    private record Pools(List<Dish> rice, List<Dish> soup, List<Dish> main, List<Dish> side, List<Dish> pickle,
                         List<Dish> dessert, List<Dish> oneDish) {

    }

    private record Forced(Dish rice, Dish soup, Dish main, Dish side, Dish pickle, Dish dessert) {

    }

    private record Candidate(double penalty, List<ComposedDish> dishes, boolean requiredMet) {

    }

}
