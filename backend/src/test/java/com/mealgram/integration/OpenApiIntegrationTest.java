package com.mealgram.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;

@EnabledIf("com.mealgram.integration.IntegrationTestSupport#dockerAvailable")
class OpenApiIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("로그인 없이 API 문서를 조회하고 API 주소에는 /api 접두사가 붙는다.")
    void returnsDocsWithoutLogin() throws Exception {

        MvcResult result = getJson("/v3/api-docs", null);
        JsonNode paths = body(result).get("paths");

        assertEquals(200, result.getResponse().getStatus());
        assertNotNull(paths.get("/api/meals"));
        assertNotNull(paths.get("/api/auth/login"));

    }

}
