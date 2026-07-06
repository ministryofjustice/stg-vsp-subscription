package uk.gov.moj.cp.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.moj.cp.service.notifications.NotificationService;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController controller;

    private static final String CASE_URN = "urn123";
    private static final String CASE_URN_UPPER = "URN123";
    private static final String USER_EMAIL = "user@example.com";

    @Test
    void shouldSendEmailNotificationWhenBasicAuthHeaderProvided() {
        final String header = "Basic " + Base64.getEncoder().encodeToString(USER_EMAIL.getBytes());

        ResponseEntity<?> response = controller.notificationToCase(CASE_URN, header);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(notificationService).sendUserSubscribedToCaseNotification(USER_EMAIL, CASE_URN_UPPER);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldUppercaseCaseUrnBeforeSendingEmailNotification() {
        final String header = "Basic " + Base64.getEncoder().encodeToString(USER_EMAIL.getBytes());

        controller.notificationToCase("mixed-Case-Urn", header);

        verify(notificationService).sendUserSubscribedToCaseNotification(USER_EMAIL, "MIXED-CASE-URN");
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailNotificationWhenAuthHeaderIsNull() {
        ResponseEntity<?> response = controller.notificationToCase(CASE_URN, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService, never()).sendUserSubscribedToCaseNotification(anyString(), anyString());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailNotificationWhenAuthHeaderIsEmpty() {
        ResponseEntity<?> response = controller.notificationToCase(CASE_URN, "");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService, never()).sendUserSubscribedToCaseNotification(anyString(), anyString());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailNotificationWhenAuthHeaderIsNotBasic() {
        ResponseEntity<?> response = controller.notificationToCase(CASE_URN, "Bearer some-jwt-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService, never()).sendUserSubscribedToCaseNotification(anyString(), anyString());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailNotificationWithEmptyEmailWhenBasicHeaderHasNoPayload() {
        ResponseEntity<?> response = controller.notificationToCase(CASE_URN, "Basic ");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).sendUserSubscribedToCaseNotification("", CASE_URN_UPPER);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendSmsNotificationWhenAuthHeaderIsNull() {
        ResponseEntity<?> response = controller.smsNotificationToCase(CASE_URN, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(notificationService).sendTextMessageForTheCaseNotification(CASE_URN_UPPER);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendSmsNotificationWhenAuthHeaderIsEmpty() {
        ResponseEntity<?> response = controller.smsNotificationToCase(CASE_URN, "");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).sendTextMessageForTheCaseNotification(CASE_URN_UPPER);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendSmsNotificationEvenWhenAuthHeaderIsPresent() {
        final String header = "Basic " + Base64.getEncoder().encodeToString(USER_EMAIL.getBytes());

        ResponseEntity<?> response = controller.smsNotificationToCase(CASE_URN, header);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).sendTextMessageForTheCaseNotification(CASE_URN_UPPER);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldUppercaseCaseUrnBeforeSendingSmsNotification() {
        controller.smsNotificationToCase("mixed-Urn", null);

        verify(notificationService).sendTextMessageForTheCaseNotification("MIXED-URN");
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNeverSendEmailFromSmsEndpoint() {
        controller.smsNotificationToCase(CASE_URN, "Basic " + Base64.getEncoder().encodeToString(USER_EMAIL.getBytes()));

        verify(notificationService, never()).sendUserSubscribedToCaseNotification(anyString(), anyString());
    }
}
