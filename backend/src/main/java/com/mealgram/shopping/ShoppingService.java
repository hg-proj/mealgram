package com.mealgram.shopping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.dish.Dish;
import com.mealgram.dish.DishRepository;
import com.mealgram.ingredient.Ingredient;
import com.mealgram.ingredient.IngredientRepository;
import com.mealgram.ingredient.MemberIngredientRepository;
import com.mealgram.shopping.dto.ShoppingItem;
import com.mealgram.shopping.dto.ShoppingListRequest;
import com.mealgram.shopping.dto.ShoppingListResponse;

// 장보기 목록 계산

@Service
public class ShoppingService {

    private static final String OTHER_CATEGORY = "기타";

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final MemberIngredientRepository memberIngredientRepository;

    public ShoppingService(DishRepository dishRepository,
                           IngredientRepository ingredientRepository,
                           MemberIngredientRepository memberIngredientRepository) {
        this.dishRepository = dishRepository;
        this.ingredientRepository = ingredientRepository;
        this.memberIngredientRepository = memberIngredientRepository;
    }

    @Transactional(readOnly = true)
    public ShoppingListResponse calculate(Long memberId, ShoppingListRequest request) {

        List<Dish> dishes = findDishes(request.dishIds());
        Set<String> owned = ownedNames(memberId, request.ownedIngredientIds());

        Map<String, List<String>> ingredientUse = new LinkedHashMap<>();
        Map<String, List<String>> seasoningUse = new LinkedHashMap<>();
        for (Dish dish : dishes) {
            dish.ingredientList().forEach(name -> use(ingredientUse, name, dish.getName()));
            dish.seasoningList().forEach(name -> use(seasoningUse, name, dish.getName()));
        }
        Map<String, String> categories = categoriesOf(ingredientUse.keySet());

        List<ShoppingItem> toBuy = new ArrayList<>();
        List<ShoppingItem> have = new ArrayList<>();
        ingredientUse.forEach((name, usedIn) -> {
            ShoppingItem item = new ShoppingItem(name, categories.getOrDefault(name, OTHER_CATEGORY), usedIn);
            (owned.contains(name) ? have : toBuy).add(item);
        });
        List<ShoppingItem> seasonings = seasoningUse.entrySet().stream()
                .map(entry -> new ShoppingItem(entry.getKey(), Ingredient.SEASONING_CATEGORY, entry.getValue()))
                .toList();

        return new ShoppingListResponse(sorted(toBuy), sorted(have), seasonings);

    }

    private List<Dish> findDishes(List<Long> dishIds) {

        Set<Long> distinct = new HashSet<>(dishIds);
        List<Dish> found = dishRepository.findAllById(distinct);
        if (found.size() != distinct.size()) {
            throw new BusinessException(ErrorCode.DISH_NOT_FOUND);
        }
        Map<Long, Dish> byId = found.stream().collect(Collectors.toMap(Dish::getId, dish -> dish));

        return dishIds.stream().distinct().map(byId::get).toList();

    }

    private Set<String> ownedNames(Long memberId, List<Long> requestedIds) {

        Set<Long> ids = new HashSet<>();
        if (requestedIds != null) {
            ids.addAll(requestedIds);
        }
        if (memberId != null) {
            ids.addAll(memberIngredientRepository.findIngredientIdsByMemberId(memberId));
        }
        if (ids.isEmpty()) {
            return Set.of();
        }
        List<Ingredient> found = ingredientRepository.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND);
        }

        return found.stream().map(Ingredient::getName).collect(Collectors.toSet());

    }

    private Map<String, String> categoriesOf(Set<String> names) {

        if (names.isEmpty()) {
            return Map.of();
        }

        return ingredientRepository.findByNameIn(names).stream()
                .collect(Collectors.toMap(Ingredient::getName, Ingredient::getCategory));

    }

    private void use(Map<String, List<String>> uses, String name, String dishName) {

        List<String> dishes = uses.computeIfAbsent(name, key -> new ArrayList<>());
        if (!dishes.contains(dishName)) {
            dishes.add(dishName);
        }

    }

    private List<ShoppingItem> sorted(List<ShoppingItem> items) {

        return items.stream()
                .sorted(Comparator.comparingInt((ShoppingItem item) -> Ingredient.categoryRank(item.category()))
                        .thenComparing(ShoppingItem::name))
                .toList();

    }

}
