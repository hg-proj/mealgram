package com.mealgram.common.mail;

import org.springframework.web.util.HtmlUtils;

// 인증번호 메일 양식, 로고와 큰 인증번호가 있는 HTML과 글자만 있는 본문

final class VerificationMailTemplate {

    private static final String GREEN = "#3e7b39";
    private static final String INK = "#1d2823";
    private static final String SUB = "#6b7670";

    record Content(String subject, String text, String html) {
    }

    private VerificationMailTemplate() {
    }

    static Content passwordReset(String nickname, String code, String siteUrl) {

        return build("mealgram의 비밀번호 재설정", greeting(nickname, "비밀번호 재설정을 완료하려면 다음 인증 코드를 입력하세요."),
                "본인이 요청하지 않은 경우 비밀번호를 변경해 계정을 보호하세요.", code, siteUrl);

    }

    static Content signup(String nickname, String code, String siteUrl) {

        return build("mealgram 가입 요청", greeting(nickname, "가입을 완료하려면 다음 인증 코드를 입력하세요."),
                "감사합니다. mealgram 드림", code, siteUrl);

    }

    private static String logo(String siteUrl) {

        if (siteUrl == null || siteUrl.isBlank()) {
            return "mealgram";
        }

        return "<a href=\"" + HtmlUtils.htmlEscape(siteUrl.strip()) + "\" target=\"_blank\" style=\"color:" + GREEN
                + ";text-decoration:none;\">mealgram</a>";

    }

    private static String greeting(String nickname, String sentence) {

        return nickname == null || nickname.isBlank() ? sentence : nickname.strip() + "님, " + sentence;

    }

    private static Content build(String subject, String greeting, String footer, String code, String siteUrl) {

        String text = """
                mealgram 인증 코드

                %s

                %s
                인증코드는 10분 후 만료됩니다.

                %s
                """.formatted(code, greeting, footer);

        String html = """
                <div style="background:#ffffff;padding:32px 16px;font-family:'Apple SD Gothic Neo','Malgun Gothic',Arial,sans-serif;color:%1$s;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:480px;margin:0 auto;text-align:center;">
                    <tr><td style="padding:12px 0 36px;font-family:Georgia,'Times New Roman',serif;font-size:44px;line-height:1;color:%2$s;">%7$s</td></tr>
                    <tr><td style="padding-bottom:14px;font-size:20px;font-weight:bold;color:%1$s;">mealgram 인증 코드</td></tr>
                    <tr><td style="padding-bottom:40px;font-size:44px;font-weight:bold;letter-spacing:2px;color:%1$s;">%3$s</td></tr>
                    <tr><td style="font-size:14px;line-height:1.8;color:%1$s;">%4$s<br>인증코드는 <span style="color:%2$s;">10분 후 만료</span>됩니다.</td></tr>
                    <tr><td style="padding-top:36px;font-size:13px;line-height:1.8;color:%5$s;">%6$s</td></tr>
                  </table>
                </div>
                """.formatted(INK, GREEN, HtmlUtils.htmlEscape(code), HtmlUtils.htmlEscape(greeting), SUB,
                HtmlUtils.htmlEscape(footer), logo(siteUrl));

        return new Content(subject, text, html);

    }

}
