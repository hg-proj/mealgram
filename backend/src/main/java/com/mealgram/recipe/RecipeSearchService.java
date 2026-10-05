package com.mealgram.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.mealgram.common.embedding.EmbeddingClient;
import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.ingredient.Ingredient;
import com.mealgram.ingredient.IngredientRepository;
import com.mealgram.recipe.dto.RecipeCandidate;

// 질문 임베딩, 벡터 검색, 필터와 조건 완화, 재정렬 처리 서비스

@Service
public class RecipeSearchService {

    private static final int VECTOR_POOL_SIZE = 100;
    private static final int CANDIDATE_SIZE = 30;
    private static final int MIN_CANDIDATE_SIZE = 15;
    private static final double OVERLAP_WEIGHT = 0.3;

    private final RecipeVectorRepository recipeVectorRepository;
    private final IngredientRepository ingredientRepository;
    private final EmbeddingClient embeddingClient;

    public RecipeSearchService(RecipeVectorRepository recipeVectorRepository,
                               IngredientRepository ingredientRepository,
                               EmbeddingClient embeddingClient) {
        this.recipeVectorRepository = recipeVectorRepository;
        this.ingredientRepository = ingredientRepository;
        this.embeddingClient = embeddingClient;
    }

    public List<RecipeCandidate> search(Long requiredId,
                                        List<Long> subIds,
                                        Long mainIngredientId,
                                        String goal,
                                        String genre) {

        List<Long> requiredIds = Stream.of(requiredId, mainIngredientId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Long> optionalIds = (subIds == null ? List.<Long>of() : subIds).stream()
                .filter(id -> !requiredIds.contains(id))
                .distinct()
                .toList();

        String queryText = RecipeQueryText.of(findNames(requiredIds, optionalIds), goal, genre);
        if (queryText.isEmpty()) {
            return recipeVectorRepository.findRandom(CANDIDATE_SIZE).stream()
                    .map(candidate -> candidate.withMatched(true))
                    .toList();
        }

        float[] vector = embeddingClient.embed(List.of(queryText)).get(0);

        List<RecipeCandidate> result = new ArrayList<>();
        Set<Long> addedIds = new HashSet<>();
        for (int minRequired = requiredIds.size(); minRequired >= 0; minRequired--) {
            if (result.size() >= MIN_CANDIDATE_SIZE) {
                break;
            }
            boolean matched = minRequired == requiredIds.size();
            rank(recipeVectorRepository.findSimilar(vector, requiredIds, minRequired, optionalIds, VECTOR_POOL_SIZE),
                    optionalIds.size()).stream()
                    .filter(candidate -> addedIds.add(candidate.id()))
                    .limit(CANDIDATE_SIZE - result.size())
                    .forEach(candidate -> result.add(candidate.withMatched(matched)));
        }

        return result.isEmpty()
                ? recipeVectorRepository.findRandom(CANDIDATE_SIZE)
                : result;

    }

    private List<RecipeCandidate> rank(List<RecipeCandidate> candidates, int optionalCount) {

        return candidates.stream()
                .sorted(Comparator.comparingDouble((RecipeCandidate candidate) -> score(candidate, optionalCount))
                        .reversed())
                .toList();

    }

    private List<String> findNames(List<Long> requiredIds, List<Long> optionalIds) {

        List<Long> ids = new ArrayList<>(requiredIds);
        ids.addAll(optionalIds);

        Map<Long, String> names = ingredientRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Ingredient::getId, Ingredient::getName, (first, second) -> first));
        if (names.size() != ids.size()) {
            throw new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND);
        }

        return ids.stream().map(names::get).toList();

    }

    private double score(RecipeCandidate candidate, int optionalCount) {

        if (optionalCount == 0) {
            return candidate.similarity();
        }

        return candidate.similarity() + OVERLAP_WEIGHT * candidate.overlapCount() / optionalCount;

    }

}
