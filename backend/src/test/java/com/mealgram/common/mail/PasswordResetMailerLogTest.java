package com.mealgram.common.mail;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class PasswordResetMailerLogTest {

    private static final String EMAIL = "secret.person@example.com";
    private static final String TOKEN = "TOKEN-VALUE-123";

    private ListAppender<ILoggingEvent> appender;
    private Logger logger;
    private ObjectProvider<JavaMailSender> provider;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        logger = (Logger) LoggerFactory.getLogger(PasswordResetMailer.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        provider = mock(ObjectProvider.class);

    }

    @AfterEach
    void tearDown() {

        logger.detachAppender(appender);

    }

    private String logged() {

        List<ILoggingEvent> events = appender.list;

        return events.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a, b) -> a + "\n" + b);

    }

    @Test
    @DisplayName("개발 설정에서 메일 설정이 없으면 재설정 링크를 로그에 남긴다.")
    void devLogsLinkWithoutMailSender() {

        when(provider.getIfAvailable()).thenReturn(null);

        new PasswordResetMailer(provider, "https://mealgram.test", "", true).send(EMAIL, TOKEN);

        assertTrue(logged().contains("https://mealgram.test/reset-password?token=" + TOKEN));

    }

    @Test
    @DisplayName("운영 설정에서 메일 설정이 없으면 링크와 이메일 없이 오류만 남긴다.")
    void prodLogsOnlyErrorWithoutMailSender() {

        when(provider.getIfAvailable()).thenReturn(null);

        new PasswordResetMailer(provider, "https://mealgram.test", "", false).send(EMAIL, TOKEN);

        assertFalse(logged().contains(TOKEN));
        assertFalse(logged().contains(EMAIL));
        assertTrue(logged().contains("메일 설정이 없어"));

    }

    @Test
    @DisplayName("메일 발송이 실패하면 이메일을 로그에 남기지 않는다.")
    void failureLogDoesNotContainEmail() {

        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(provider.getIfAvailable()).thenReturn(mailSender);
        doThrow(new MailSendException("연결 실패")).when(mailSender).send(any(SimpleMailMessage.class));

        new PasswordResetMailer(provider, "https://mealgram.test", "", false).send(EMAIL, TOKEN);

        assertFalse(logged().contains(EMAIL));
        assertFalse(logged().contains(TOKEN));
        assertTrue(logged().contains("재설정 메일 발송에 실패했습니다"));

    }

}
