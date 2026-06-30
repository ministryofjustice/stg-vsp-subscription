package uk.gov.moj.cp.service.notifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private EmailService emailService;

    @Test
    void shouldDelegateToNotificationClient() throws NotificationClientException {
        Map<String, String> params = Map.of("email", "user@example.com", "caseurn", "URN1");

        emailService.sendMail("target@example.com", "template-id", params, "ref-1");

        verify(notificationClient).sendEmail("template-id", "target@example.com", params, "ref-1");
    }

    @Test
    void shouldAllowNullReference() throws NotificationClientException {
        Map<String, String> params = Map.of();

        emailService.sendMail("target@example.com", "template-id", params, null);

        verify(notificationClient).sendEmail("template-id", "target@example.com", params, null);
    }

    @Test
    void shouldPropagateNotificationClientException() throws NotificationClientException {
        doThrow(new NotificationClientException("API down"))
            .when(notificationClient).sendEmail(any(), any(), any(), any());

        assertThatThrownBy(() ->
            emailService.sendMail("t@e.com", "tpl", Map.of(), "r"))
            .isInstanceOf(NotificationClientException.class)
            .hasMessageContaining("API down");
    }
}
