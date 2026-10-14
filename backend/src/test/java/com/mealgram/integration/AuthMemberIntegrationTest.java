package com.mealgram.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;

@EnabledIf("com.mealgram.integration.IntegrationTestSupport#dockerAvailable")
class AuthMemberIntegrationTest extends IntegrationTestSupport {

    private int status(MvcResult result) {

        return result.getResponse().getStatus();

    }

    private String errorCode(MvcResult result) throws Exception {

        return body(result).get("error").get("code").asString();

    }

    @Test
    @DisplayName("가입하고 로그인해서 내정보를 조회하면 이메일은 소문자로 저장되어 있다.")
    void signupLoginAndReadProfile() throws Exception {

        String loginId = unique("flow");
        MvcResult signup = postJson("/auth/signup", Map.of("nickname", "한결", "loginId", loginId, "password", password(),
                "email", loginId.toUpperCase() + "@Example.COM", "age", 30, "gender", "FEMALE", "height", 160,
                "weight", 55, "activityLevel", "LIGHT"), null);
        assertEquals(201, status(signup));

        JsonNode login = body(postJson("/auth/login", Map.of("loginId", loginId, "password", password()), null));
        JsonNode me = body(getJson("/members/me", login.get("accessToken").asString()));

        assertEquals(loginId, me.get("loginId").asString());
        assertEquals(loginId + "@example.com", me.get("email").asString());
        assertEquals(30, me.get("age").asInt());
        assertFalse(me.has("password"));

    }

    @Test
    @DisplayName("아이디가 같거나 대소문자만 다른 이메일로 가입하면 409로 거절한다.")
    void rejectsDuplicateSignup() throws Exception {

        Member member = signupAndLogin("dup");

        MvcResult sameLoginId = postJson("/auth/signup", Map.of("nickname", "중복", "loginId", member.loginId(),
                "password", password(), "email", unique("other") + "@example.com"), null);
        MvcResult sameEmail = postJson("/auth/signup", Map.of("nickname", "중복", "loginId", unique("dup"),
                "password", password(), "email", member.email().toUpperCase()), null);

        assertEquals(409, status(sameLoginId));
        assertEquals("DUPLICATE_LOGIN_ID", errorCode(sameLoginId));
        assertEquals(409, status(sameEmail));
        assertEquals("DUPLICATE_EMAIL", errorCode(sameEmail));

    }

    @Test
    @DisplayName("입력 규칙에 맞지 않으면 필드별 오류와 함께 400으로 거절한다.")
    void rejectsInvalidInput() throws Exception {

        MvcResult result = postJson("/auth/signup", Map.of("nickname", "한", "loginId", "ab", "password", "aaaaaaaa",
                "email", "not-an-email", "age", 2, "height", 251, "weight", 9), null);

        assertEquals(400, status(result));
        JsonNode error = body(result).get("error");
        assertEquals("VALIDATION_FAILED", error.get("code").asString());
        String fields = error.get("fieldErrors").toString();
        for (String field : new String[] {"nickname", "loginId", "password", "email", "age", "height", "weight"}) {
            assertTrue(fields.contains("\"" + field + "\""), field + " 오류가 없음: " + fields);
        }

    }

    @Test
    @DisplayName("내정보를 일부만 수정할 수 있고 잘못된 값은 거절한다.")
    void updatesProfile() throws Exception {

        Member member = signupAndLogin("upd");

        JsonNode updated = body(patchJson("/members/me", Map.of("nickname", "새닉네임", "weight", 53.5), member.accessToken()));
        assertEquals("새닉네임", updated.get("nickname").asString());
        assertEquals(30, updated.get("age").asInt());
        assertEquals(53.5, updated.get("weight").asDouble());

        assertEquals(400, status(patchJson("/members/me", Map.of("nickname", "          "), member.accessToken())));
        assertEquals(400, status(patchJson("/members/me", Map.of("age", 9999), member.accessToken())));

    }

    @Test
    @DisplayName("회원탈퇴하면 이후 조회는 404이고 재발급도 되지 않는다.")
    void deleteAccount() throws Exception {

        Member member = signupAndLogin("del");

        assertEquals(204, status(deleteJson("/members/me", member.accessToken())));
        assertEquals(404, status(getJson("/members/me", member.accessToken())));
        assertEquals(401, status(postJson("/auth/refresh", Map.of("refreshToken", member.refreshToken()), null)));
        assertEquals(401, status(postJson("/auth/login", Map.of("loginId", member.loginId(), "password", password()), null)));

    }

    @Test
    @DisplayName("토큰 종류를 구분해 재발급과 로그아웃을 처리한다.")
    void tokenFlow() throws Exception {

        Member member = signupAndLogin("tok");

        MvcResult refreshed = postJson("/auth/refresh", Map.of("refreshToken", member.refreshToken()), null);
        assertEquals(200, status(refreshed));
        assertEquals(200, status(getJson("/members/me", body(refreshed).get("accessToken").asString())));

        assertEquals(401, status(postJson("/auth/refresh", Map.of("refreshToken", member.accessToken()), null)));
        assertEquals(401, status(getJson("/members/me", member.refreshToken())));

        assertEquals(204, status(postJson("/auth/logout", Map.of("refreshToken", member.refreshToken()), null)));
        assertEquals(401, status(postJson("/auth/refresh", Map.of("refreshToken", member.refreshToken()), null)));
        assertEquals(204, status(postJson("/auth/logout", Map.of("refreshToken", member.refreshToken()), null)));

    }

    @Test
    @DisplayName("새로 로그인하면 이전 refreshToken은 쓸 수 없다.")
    void newLoginInvalidatesOldRefreshToken() throws Exception {

        Member member = signupAndLogin("one");
        JsonNode second = body(postJson("/auth/login", Map.of("loginId", member.loginId(), "password", password()), null));

        assertEquals(401, status(postJson("/auth/refresh", Map.of("refreshToken", member.refreshToken()), null)));
        assertEquals(200, status(postJson("/auth/refresh", Map.of("refreshToken", second.get("refreshToken").asString()), null)));

    }

    @Test
    @DisplayName("비밀번호 재설정은 한 번만 쓸 수 있는 링크로 새 비밀번호를 정하고 이전 로그인은 무효가 된다.")
    void passwordReset() throws Exception {

        Member member = signupAndLogin("rst");

        assertEquals(200, status(postJson("/auth/forgot-password", Map.of("email", member.email().toUpperCase()), null)));
        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(passwordResetMailer).send(eq(member.email()), token.capture());
        assertNotNull(token.getValue());

        assertEquals(400, status(postJson("/auth/reset-password", Map.of("token", token.getValue(), "newPassword", "short"), null)));
        assertEquals(200, status(postJson("/auth/reset-password", Map.of("token", token.getValue(), "newPassword", "New-pass-98!"), null)));
        assertEquals(400, status(postJson("/auth/reset-password", Map.of("token", token.getValue(), "newPassword", "Other-pass-1!"), null)));

        assertEquals(401, status(postJson("/auth/login", Map.of("loginId", member.loginId(), "password", password()), null)));
        assertEquals(200, status(postJson("/auth/login", Map.of("loginId", member.loginId(), "password", "New-pass-98!"), null)));
        assertEquals(401, status(postJson("/auth/refresh", Map.of("refreshToken", member.refreshToken()), null)));

    }

    @Test
    @DisplayName("가입하지 않은 이메일도 200으로 응답하고 메일은 보내지 않는다.")
    void forgotPasswordWithUnknownEmail() throws Exception {

        assertEquals(200, status(postJson("/auth/forgot-password", Map.of("email", unique("nobody") + "@example.com"), null)));

        verify(passwordResetMailer, never()).send(eq("nobody@example.com"), anyString());

    }

    @Test
    @DisplayName("같은 아이디로 5번 실패하면 맞는 비밀번호도 429로 막고 다른 아이디는 영향이 없다.")
    void loginIsBlockedAfterRepeatedFailures() throws Exception {

        Member victim = signupAndLogin("blk");
        Member other = signupAndLogin("blo");

        for (int i = 0; i < 5; i++) {
            assertEquals(401, status(postJson("/auth/login", Map.of("loginId", victim.loginId(), "password", "Wrong-pass-" + i + "!"), null)));
        }
        MvcResult blocked = postJson("/auth/login", Map.of("loginId", victim.loginId(), "password", password()), null);

        assertEquals(429, status(blocked));
        assertEquals("LOGIN_BLOCKED", errorCode(blocked));
        assertEquals(200, status(postJson("/auth/login", Map.of("loginId", other.loginId(), "password", password()), null)));

    }

    @Test
    @DisplayName("로그인 실패와 없는 아이디는 같은 오류 메시지로 응답한다.")
    void sameErrorForWrongPasswordAndUnknownLoginId() throws Exception {

        Member member = signupAndLogin("same");

        MvcResult wrongPassword = postJson("/auth/login", Map.of("loginId", member.loginId(), "password", "Wrong-pass-1!"), null);
        MvcResult unknown = postJson("/auth/login", Map.of("loginId", unique("ghost"), "password", "Wrong-pass-1!"), null);

        assertEquals(401, status(wrongPassword));
        assertEquals(401, status(unknown));
        assertEquals(body(wrongPassword).toString(), body(unknown).toString());

    }

    @Test
    @DisplayName("오류는 모두 같은 공통 형식으로 응답한다.")
    void errorsUseCommonFormat() throws Exception {

        Member member = signupAndLogin("err");
        String token = member.accessToken();

        MvcResult unauthorized = getJson("/members/me", null);
        MvcResult notFound = getJson("/nothing", token);
        MvcResult methodNotAllowed = mockMvc.perform(put("/members/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andReturn();
        MvcResult unsupportedMedia = mockMvc.perform(post("/saved-meals").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.TEXT_PLAIN).content("recipeId=1")).andReturn();
        MvcResult badPathVariable = getJson("/recipes/abc", token);
        MvcResult brokenJson = mockMvc.perform(post("/saved-meals").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content("{broken")).andReturn();

        Map<MvcResult, String[]> expected = new LinkedHashMap<>();
        expected.put(unauthorized, new String[] {"401", "UNAUTHORIZED"});
        expected.put(notFound, new String[] {"404", "RESOURCE_NOT_FOUND"});
        expected.put(methodNotAllowed, new String[] {"405", "METHOD_NOT_ALLOWED"});
        expected.put(unsupportedMedia, new String[] {"415", "UNSUPPORTED_MEDIA_TYPE"});
        expected.put(badPathVariable, new String[] {"400", "INVALID_REQUEST"});
        expected.put(brokenJson, new String[] {"400", "INVALID_REQUEST"});
        for (Map.Entry<MvcResult, String[]> entry : expected.entrySet()) {
            JsonNode json = body(entry.getKey());
            assertEquals(Integer.parseInt(entry.getValue()[0]), status(entry.getKey()), json.toString());
            assertFalse(json.get("success").asBoolean(), json.toString());
            assertEquals(entry.getValue()[1], json.get("error").get("code").asString(), json.toString());
            assertFalse(json.has("timestamp"), json.toString());
        }

    }

    @Test
    @DisplayName("프론트 주소의 사전 요청은 허용하고 다른 주소는 거절한다.")
    void corsPreflight() throws Exception {

        MvcResult allowed = mockMvc.perform(options("/auth/login").header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,authorization")).andReturn();
        MvcResult denied = mockMvc.perform(options("/auth/login").header(HttpHeaders.ORIGIN, "https://evil.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")).andReturn();

        assertEquals(200, status(allowed));
        assertEquals("http://localhost:5173", allowed.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals(403, status(denied));

    }

    @Test
    @DisplayName("헬스 체크는 로그인 없이 UP이고 다른 관리 엔드포인트는 공개하지 않는다.")
    void healthIsPublicOthersAreNot() throws Exception {

        MvcResult health = mockMvc.perform(get("/actuator/health")).andReturn();
        MvcResult env = mockMvc.perform(get("/actuator/env")).andReturn();

        assertEquals(200, status(health));
        assertEquals("UP", body(health).get("status").asString());
        assertEquals(401, status(env));

    }

}
