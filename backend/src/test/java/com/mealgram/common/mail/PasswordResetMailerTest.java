package com.mealgram.common.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.mealgram.common.exception.BusinessException;
import com.mealgram.common.exception.ErrorCode;

import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

class PasswordResetMailerTest {

    private ObjectProvider<JavaMailSender> provider;
    private JavaMailSender mailSender;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {

        provider = mock(ObjectProvider.class);
        mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage((Session) null));

    }

    // 메일 본문 조각을 글자로 모음, 글자만 있는 본문과 HTML 본문이 함께 나옴
    private static List<String> bodies(MimeMessage message) throws Exception {

        List<String> out = new ArrayList<>();
        collect(message.getContent(), out);

        return out;

    }

    private static void collect(Object content, List<String> out) throws Exception {

        if (content instanceof String text) {
            out.add(text);
        } else if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                collect(multipart.getBodyPart(i).getContent(), out);
            }
        }

    }

    private MimeMessage sent() {

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        return captor.getValue();

    }

    @Test
    @DisplayName("메일 설정이 있으면 인증번호가 담긴 HTML 메일과 글자만 있는 본문을 함께 보낸다.")
    void sendsHtmlAndTextMail() throws Exception {

        when(provider.getIfAvailable()).thenReturn(mailSender);
        PasswordResetMailer mailer = new PasswordResetMailer(provider, "noreply@mealgram.test", false, "https://mealgram.test");

        mailer.send("a@example.com", "한결", "123456");

        MimeMessage message = sent();
        InternetAddress from = (InternetAddress) message.getFrom()[0];
        List<String> bodies = bodies(message);

        assertEquals("mealgram의 비밀번호 재설정", message.getSubject());
        assertEquals("a@example.com", message.getAllRecipients()[0].toString());
        assertEquals("noreply@mealgram.test", from.getAddress());
        assertEquals("mealgram", from.getPersonal());
        assertEquals(2, bodies.size());
        assertTrue(bodies.stream().allMatch(body -> body.contains("123456") && body.contains("한결님")));
        assertTrue(bodies.stream().anyMatch(body -> body.contains("<span") && body.contains("10분 후 만료")));

    }

    @Test
    @DisplayName("보내는 주소 설정이 없으면 보내는 주소를 지정하지 않는다.")
    void omitsFromWhenBlank() throws Exception {

        when(provider.getIfAvailable()).thenReturn(mailSender);
        PasswordResetMailer mailer = new PasswordResetMailer(provider, "", false, "https://mealgram.test");

        mailer.send("a@example.com", "한결", "123456");

        assertNull(sent().getFrom());

    }

    @Test
    @DisplayName("가입 인증번호 메일은 제목과 문구가 다르고 인증번호가 담긴다.")
    void sendsSignupCodeMail() throws Exception {

        when(provider.getIfAvailable()).thenReturn(mailSender);
        PasswordResetMailer mailer = new PasswordResetMailer(provider, "noreply@mealgram.test", false, "https://mealgram.test");

        mailer.sendSignupCode("a@example.com", "한결", "654321");

        MimeMessage message = sent();
        List<String> bodies = bodies(message);

        assertEquals("mealgram 가입 요청", message.getSubject());
        assertTrue(bodies.stream().allMatch(body -> body.contains("654321") && body.contains("가입을 완료하려면")));
        assertTrue(bodies.stream().allMatch(body -> body.contains("감사합니다. mealgram 드림")));

    }

    @Test
    @DisplayName("메일 설정이 없고 로그 남기기도 꺼져 있으면 발송 실패로 알린다.")
    void failsWithoutMailSender() {

        when(provider.getIfAvailable()).thenReturn(null);
        PasswordResetMailer mailer = new PasswordResetMailer(provider, "", false, "https://mealgram.test");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> mailer.send("a@example.com", "한결", "123456"));

        assertEquals(ErrorCode.MAIL_SEND_FAILED, exception.getErrorCode());
        verify(mailSender, never()).send(any(MimeMessage.class));

    }

    @Test
    @DisplayName("메일 발송이 실패하면 발송 실패로 알린다.")
    void reportsMailFailure() {

        when(provider.getIfAvailable()).thenReturn(mailSender);
        doThrow(new MailSendException("실패")).when(mailSender).send(any(MimeMessage.class));
        PasswordResetMailer mailer = new PasswordResetMailer(provider, "", false, "https://mealgram.test");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> mailer.send("a@example.com", "한결", "123456"));

        assertEquals(ErrorCode.MAIL_SEND_FAILED, exception.getErrorCode());

    }

    @Test
    @DisplayName("서버 주소가 비어 있는 메일 설정은 설정이 없는 것으로 본다.")
    void treatsBlankHostAsNotConfigured() {

        when(provider.getIfAvailable()).thenReturn(new JavaMailSenderImpl());
        PasswordResetMailer mailer = new PasswordResetMailer(provider, "", false, "https://mealgram.test");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> mailer.send("a@example.com", "한결", "123456"));

        assertEquals(ErrorCode.MAIL_SEND_FAILED, exception.getErrorCode());

    }

}
