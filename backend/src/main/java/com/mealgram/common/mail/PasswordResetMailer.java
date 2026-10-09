package com.mealgram.common.mail;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

// 비밀번호 재설정 메일 발송

@Slf4j
@Component
public class PasswordResetMailer {

    private static final String SUBJECT = "[mealgram] 비밀번호 재설정 안내";
    private static final String BODY = """
            mealgram 비밀번호 재설정 링크입니다.
            아래 링크에서 30분 안에 새 비밀번호를 설정해 주세요.

            %s

            본인이 요청하지 않았다면 이 메일을 무시해 주세요.
            """;

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String frontendUrl;
    private final String from;

    public PasswordResetMailer(ObjectProvider<JavaMailSender> mailSenderProvider,
                               @Value("${app.frontend-url}") String frontendUrl,
                               @Value("${spring.mail.username:}") String from) {
        this.mailSenderProvider = mailSenderProvider;
        this.frontendUrl = frontendUrl;
        this.from = from;
    }

    public void send(String email, String token) {

        String link = frontendUrl + "/reset-password?token=" + token;
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("메일 설정이 없어 재설정 링크를 로그로만 남깁니다. 수신 {}, 링크 {}", email, link);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (!from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(email);
        message.setSubject(SUBJECT);
        message.setText(BODY.formatted(link));

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("재설정 메일 발송에 실패했습니다. 수신 {}", email, e);
        }

    }

}
