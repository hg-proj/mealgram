package com.mealgram.common.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mealgram.common.mail.VerificationMailTemplate.Content;

class VerificationMailTemplateTest {

    @Test
    @DisplayName("비밀번호 재설정 메일에는 로고와 인증번호와 안내 문구가 들어간다.")
    void passwordResetContent() {

        Content content = VerificationMailTemplate.passwordReset("한결", "549854", "https://mealgram.test");

        assertEquals("mealgram의 비밀번호 재설정", content.subject());
        assertTrue(content.html().contains("<a href=\"https://mealgram.test\""));
        assertTrue(content.html().contains("mealgram 인증 코드"));
        assertTrue(content.html().contains("549854"));
        assertTrue(content.html().contains("한결님, 비밀번호 재설정을 완료하려면 다음 인증 코드를 입력하세요."));
        assertTrue(content.html().contains("<span style=\"color:#3e7b39;\">10분 후 만료</span>됩니다."));
        assertTrue(content.html().contains("본인이 요청하지 않은 경우 비밀번호를 변경해 계정을 보호하세요."));
        assertTrue(content.text().contains("인증코드는 10분 후 만료됩니다."));

    }

    @Test
    @DisplayName("가입 메일은 가입 문구와 맺음말을 쓴다.")
    void signupContent() {

        Content content = VerificationMailTemplate.signup("한결", "123456", "https://mealgram.test");

        assertEquals("mealgram 가입 요청", content.subject());
        assertTrue(content.html().contains("한결님, 가입을 완료하려면 다음 인증 코드를 입력하세요."));
        assertTrue(content.html().contains("감사합니다. mealgram 드림"));
        assertTrue(content.text().contains("감사합니다. mealgram 드림"));

    }

    @Test
    @DisplayName("사이트 주소가 없으면 로고에 링크를 걸지 않는다.")
    void omitsLinkWhenNoSiteUrl() {

        assertFalse(VerificationMailTemplate.signup("한결", "123456", "").html().contains("<a "));

    }

    @Test
    @DisplayName("이름이 없으면 이름 없이 문장만 쓴다.")
    void omitsNameWhenBlank() {

        assertTrue(VerificationMailTemplate.signup(null, "123456", "https://mealgram.test").text().contains("\n가입을 완료하려면 다음 인증 코드를 입력하세요."));
        assertTrue(VerificationMailTemplate.signup("  ", "123456", "https://mealgram.test").text().contains("\n가입을 완료하려면 다음 인증 코드를 입력하세요."));

    }

    @Test
    @DisplayName("이름에 들어 있는 HTML 태그는 글자로 바꿔 메일에 넣는다.")
    void escapesNameInHtml() {

        Content content = VerificationMailTemplate.signup("<b>x</b>", "123456", "https://mealgram.test");

        assertFalse(content.html().contains("<b>x</b>"));
        assertTrue(content.html().contains("&lt;b&gt;x&lt;/b&gt;님"));

    }

}
