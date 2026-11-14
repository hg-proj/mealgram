package com.mealgram.common.mail;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

// 비밀번호 재설정 인증번호 메일 발송

@Slf4j
@Component
public class PasswordResetMailer {

    private static final String SUBJECT = "[mealgram] 비밀번호 재설정 인증번호";
    private static final String BODY = """
            mealgram 비밀번호 재설정 인증번호입니다.
            아래 인증번호를 10분 안에 입력해 주세요.

            %s

            본인이 요청하지 않았다면 이 메일을 무시해 주세요.
            """;

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final boolean logResetCode;

    public PasswordResetMailer(ObjectProvider<JavaMailSender> mailSenderProvider,
                               @Value("${spring.mail.username:}") String from,
                               @Value("${app.reset-code-log:false}") boolean logResetCode) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.logResetCode = logResetCode;
    }

    public void send(String email, String code) {

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            if (logResetCode) {
                log.info("메일 설정이 없어 인증번호를 로그로만 남깁니다. 수신 {}, 인증번호 {}", email, code);
            } else {
                log.error("메일 설정이 없어 재설정 메일을 보내지 못했습니다.");
            }
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (!from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(email);
        message.setSubject(SUBJECT);
        message.setText(BODY.formatted(code));

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("재설정 메일 발송에 실패했습니다: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
        }

    }

}
