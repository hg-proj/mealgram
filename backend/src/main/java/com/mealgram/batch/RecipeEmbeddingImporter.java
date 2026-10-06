package com.mealgram.batch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.mealgram.common.embedding.EmbeddingClient;
import com.mealgram.recipe.RecipeEmbeddingText;

import lombok.extern.slf4j.Slf4j;

// 레시피 임베딩 생성/저장 배치

@Slf4j
@Component
@Order(2)
@ConditionalOnProperty(name = "recipe.embedding.enabled", havingValue = "true")
public class RecipeEmbeddingImporter implements ApplicationRunner {

    private static final int BATCH_SIZE = 100;

    private static final String SELECT_TARGETS = """
            select r.id, r.name, r.category, r.cooking_method,
                   array_remove(array_agg(i.name order by i.id), null) as ingredients
            from recipe r
            left join recipe_ingredient ri on ri.recipe_id = r.id
            left join ingredient i on i.id = ri.ingredient_id
            where r.embedding is null
            group by r.id
            order by r.id
            """;
    private static final String UPDATE_EMBEDDING = "update recipe set embedding = ?::vector where id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingClient embeddingClient;

    public RecipeEmbeddingImporter(JdbcTemplate jdbcTemplate, EmbeddingClient embeddingClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingClient = embeddingClient;
    }

    @Override
    public void run(ApplicationArguments args) {

        List<EmbeddingTarget> targets = jdbcTemplate.query(SELECT_TARGETS, this::toTarget);
        if (targets.isEmpty()) {
            log.info("임베딩이 필요한 레시피가 없어서 건너뜁니다.");
            return;
        }

        int saved = 0;
        for (int from = 0; from < targets.size(); from += BATCH_SIZE) {
            List<EmbeddingTarget> batch = targets.subList(from, Math.min(from + BATCH_SIZE, targets.size()));

            saveEmbeddings(batch);
            saved += batch.size();
            log.info("임베딩 저장 {} / {}", saved, targets.size());
        }

    }

    private void saveEmbeddings(List<EmbeddingTarget> batch) {

        List<float[]> vectors = embeddingClient.embed(batch.stream().map(EmbeddingTarget::text).toList());

        List<Object[]> params = new ArrayList<>();
        for (int i = 0; i < batch.size(); i++) {
            params.add(new Object[] {Arrays.toString(vectors.get(i)), batch.get(i).id()});
        }
        jdbcTemplate.batchUpdate(UPDATE_EMBEDDING, params);

    }

    private EmbeddingTarget toTarget(ResultSet rs, int rowNum) throws SQLException {

        List<String> ingredients = Arrays.asList((String[]) rs.getArray("ingredients").getArray());
        String text = RecipeEmbeddingText.of(rs.getString("name"), rs.getString("category"),
                rs.getString("cooking_method"), ingredients);

        return new EmbeddingTarget(rs.getLong("id"), text);

    }

    private record EmbeddingTarget(long id, String text) {

    }

}
