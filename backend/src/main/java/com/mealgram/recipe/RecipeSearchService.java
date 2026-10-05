package com.mealgram.recipe;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mealgram.common.embedding.EmbeddingClient;
import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.ingredient.Ingredient;
import com.mealgram.ingredient.IngredientRepository;
import com.mealgram.recipe.dto.RecipeCandidate;

// 질문 임베딩, 벡터 검색, 필터와 재정렬 처리 서비스

@Service
public class RecipeSearchService {

    private static final int CANDIDATE_SIZE = 30;

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

    public List<RecipeCandidate> search(List<Long> ingredientIds, String goal, String genre) {

        List<Long> ids = ingredientIds == null ? List.of() : ingredientIds.stream().distinct().toList();

        String queryText = RecipeQueryText.of(findNames(ids), goal, genre);
        if (queryText.isEmpty()) {
            return recipeVectorRepository.findRandom(CANDIDATE_SIZE);
        }

        float[] vector = embeddingClient.embed(List.of(queryText)).get(0);

        return recipeVectorRepository.findSimilar(vector, CANDIDATE_SIZE);

    }

    private List<String> findNames(List<Long> ids) {

        Map<Long, String> names = ingredientRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Ingredient::getId, Ingredient::getName, (first, second) -> first));
        if (names.size() != ids.size()) {
            throw new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND);
        }

        return ids.stream().map(names::get).toList();

    }

}
