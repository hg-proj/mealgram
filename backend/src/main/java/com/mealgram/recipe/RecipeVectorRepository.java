package com.mealgram.recipe;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mealgram.recipe.dto.RecipeCandidate;

// 레시피 벡터 유사도 검색 인터페이스

@Repository
public class RecipeVectorRepository {

    private static final long NO_MATCH_ID = -1L;

    private static final String SELECT_COLUMNS = """
            select r.id, r.name, r.category, r.cooking_method, r.calorie, r.carbohydrate,
                   r.protein, r.fat, r.sodium,
                   coalesce((select array_agg(i.name order by i.id)
                               from recipe_ingredient ri
                               join ingredient i on i.id = ri.ingredient_id
                              where ri.recipe_id = r.id), '{}') as ingredients,
            """;

    private static final String SIMILAR_SQL = SELECT_COLUMNS + """
                   1 - (r.embedding <=> cast(:vector as vector)) as similarity,
                   (select count(*) from recipe_ingredient ri
                     where ri.recipe_id = r.id and ri.ingredient_id in (:optionalIds)) as overlap
              from recipe r
             where r.embedding is not null
               and (select count(*) from recipe_ingredient ri
                     where ri.recipe_id = r.id and ri.ingredient_id in (:requiredIds)) >= :minRequiredCount
             order by r.embedding <=> cast(:vector as vector)
             limit :limit
            """;

    private static final String RANDOM_SQL = SELECT_COLUMNS + """
                   0.0 as similarity,
                   0 as overlap
              from recipe r
             order by random()
             limit :limit
            """;

    private static final String CATEGORY_SIMILAR_SQL = SELECT_COLUMNS + """
                   1 - (r.embedding <=> cast(:vector as vector)) as similarity,
                   0 as overlap
              from recipe r
             where r.embedding is not null and r.category = :category
             order by r.embedding <=> cast(:vector as vector)
             limit :limit
            """;

    private static final String CATEGORY_RANDOM_SQL = SELECT_COLUMNS + """
                   0.0 as similarity,
                   0 as overlap
              from recipe r
             where r.category = :category
             order by random()
             limit :limit
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RecipeVectorRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RecipeCandidate> findSimilar(float[] vector,
                                             List<Long> requiredIds,
                                             int minRequiredCount,
                                             List<Long> optionalIds,
                                             int limit) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("vector", Arrays.toString(vector))
                .addValue("requiredIds", orNoMatch(requiredIds))
                .addValue("minRequiredCount", minRequiredCount)
                .addValue("optionalIds", orNoMatch(optionalIds))
                .addValue("limit", limit);

        return jdbcTemplate.query(SIMILAR_SQL, params, this::toCandidate);

    }

    public List<RecipeCandidate> findByCategory(float[] vector, String category, int limit) {

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("category", category)
                .addValue("limit", limit);
        if (vector == null) {
            return jdbcTemplate.query(CATEGORY_RANDOM_SQL, params, this::toCandidate);
        }
        params.addValue("vector", Arrays.toString(vector));

        return jdbcTemplate.query(CATEGORY_SIMILAR_SQL, params, this::toCandidate);

    }

    public List<RecipeCandidate> findRandom(int limit) {

        return jdbcTemplate.query(RANDOM_SQL, new MapSqlParameterSource("limit", limit), this::toCandidate);

    }

    private List<Long> orNoMatch(List<Long> ids) {

        return ids.isEmpty() ? List.of(NO_MATCH_ID) : ids;

    }

    private RecipeCandidate toCandidate(ResultSet rs, int rowNum) throws SQLException {

        return new RecipeCandidate(rs.getLong("id"), rs.getString("name"), rs.getString("category"),
                rs.getString("cooking_method"), rs.getBigDecimal("calorie"), rs.getBigDecimal("carbohydrate"),
                rs.getBigDecimal("protein"), rs.getBigDecimal("fat"), rs.getBigDecimal("sodium"),
                Arrays.asList((String[]) rs.getArray("ingredients").getArray()),
                rs.getDouble("similarity"), rs.getLong("overlap"), false);

    }

}
