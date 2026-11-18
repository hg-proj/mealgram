package com.mealgram.common.mail;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;
import com.mealgram.common.mail.VerificationMailTemplate.Content;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

// 인증번호 메일 발송, 비밀번호 재설정과 회원가입

@Slf4j
@Component
public class PasswordResetMailer {

    private static final String SENDER_NAME = "mealgram";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final boolean logResetCode;
    private final String siteUrl;

    public PasswordResetMailer(ObjectProvider<JavaMailSender> mailSenderProvider,
                               @Value("${spring.mail.username:}") String from,
                               @Value("${app.reset-code-log:false}") boolean logResetCode,
                               @Value("${app.frontend-url:}") String siteUrl) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.logResetCode = logResetCode;
        this.siteUrl = siteUrl;
    }

    @PostConstruct
    void reportConfiguration() {

        if (configuredSender() != null) {
            return;
        }

        if (logResetCode) {
            log.info("메일 설정이 없어 인증번호를 로그로만 남깁니다.");
        } else {
            log.warn("메일 설정이 없어 인증번호 메일을 보낼 수 없습니다. "
                    + "SPRING_MAIL_HOST, SPRING_MAIL_USERNAME, SPRING_MAIL_PASSWORD 환경변수를 확인해 주세요.");
        }

    }

    public void send(String email, String nickname, String code) {

        deliver(email, code, VerificationMailTemplate.passwordReset(nickname, code, siteUrl));

    }

    public void sendSignupCode(String email, String nickname, String code) {

        deliver(email, code, VerificationMailTemplate.signup(nickname, code, siteUrl));

    }

    private void deliver(String email, String code, Content content) {

        JavaMailSender mailSender = configuredSender();

        if (mailSender == null) {
            if (logResetCode) {
                log.info("메일 설정이 없어 인증번호를 로그로만 남깁니다. 수신 {}, 인증번호 {}", email, code);

                return;
            }
            log.error("메일 설정이 없어 인증번호 메일을 보내지 못했습니다.");

            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (!from.isBlank()) {
                helper.setFrom(new InternetAddress(from, SENDER_NAME, "UTF-8"));
            }
            helper.setTo(email);
            helper.setSubject(content.subject());
            helper.setText(content.text(), content.html());

            mailSender.send(message);
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.error("인증번호 메일 발송에 실패했습니다: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());

            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }

    }

    private JavaMailSender configuredSender() {

        JavaMailSender sender = mailSenderProvider.getIfAvailable();

        if (sender instanceof JavaMailSenderImpl impl && (impl.getHost() == null || impl.getHost().isBlank())) {
            return null;
        }

        return sender;

    }

}
