package com.mealgram.common.mail;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class PasswordResetMailerTest {

    private ObjectProvider<JavaMailSender> provider;
    private JavaMailSender mailSender;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        provider = mock(ObjectProvider.class);
        mailSender = mock(JavaMailSender.class);

    }

    @Test
    @DisplayName("메일 설정이 있으면 재설정 링크가 담긴 메일을 보낸다.")
    void sendsMailWithLink() {

        when(provider.getIfAvailable()).thenReturn(mailSender);
        PasswordResetMailer mailer = new PasswordResetMailer(provider, "https://mealgram.test", "noreply@mealgram.test");

        mailer.send("a@example.com", "TOKEN123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("noreply@mealgram.test", message.getFrom());
        assertEquals("a@example.com", message.getTo()[0]);
        assertTrue(message.getText().contains("https://mealgram.test/reset-password?token=TOKEN123"));

    }

    @Test
    @DisplayName("보내는 주소 설정이 없으면 보내는 주소를 지정하지 않는다.")
    void omitsFromWhenBlank() {

        when(provider.getIfAvailable()).thenReturn(mailSender);
        PasswordResetMailer mailer = new PasswordResetMailer(provider, "https://mealgram.test", "");

        mailer.send("a@example.com", "TOKEN123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertEquals(null, captor.getValue().getFrom());

    }

    @Test
    @DisplayName("메일 설정이 없으면 오류 없이 로그만 남긴다.")
    void doesNothingWithoutMailSender() {

        when(provider.getIfAvailable()).thenReturn(null);
        PasswordResetMailer mailer = new PasswordResetMailer(provider, "https://mealgram.test", "");

        assertDoesNotThrow(() -> mailer.send("a@example.com", "TOKEN123"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));

    }

    @Test
    @DisplayName("메일 발송이 실패해도 예외를 밖으로 내보내지 않는다.")
    void swallowsMailFailure() {

        when(provider.getIfAvailable()).thenReturn(mailSender);
        doThrow(new MailSendException("실패")).when(mailSender).send(any(SimpleMailMessage.class));
        PasswordResetMailer mailer = new PasswordResetMailer(provider, "https://mealgram.test", "");

        assertDoesNotThrow(() -> mailer.send("a@example.com", "TOKEN123"));

    }

}
