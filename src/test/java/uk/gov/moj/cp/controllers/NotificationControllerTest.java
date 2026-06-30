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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
    void shouldSendNotificationWhenBasicAuthHeaderProvided() {
        final String header = "Basic " + Base64.getEncoder().encodeToString(USER_EMAIL.getBytes());

        ResponseEntity<?> response = controller.notificationToCase(CASE_URN, header);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).sendUserSubscribedToCaseNotification(USER_EMAIL, CASE_URN_UPPER);
    }

    @Test
    void shouldUppercaseCaseUrnBeforeSendingNotification() {
        final String header = "Basic " + Base64.getEncoder().encodeToString(USER_EMAIL.getBytes());

        controller.notificationToCase("mixed-Case-Urn", header);

        verify(notificationService).sendUserSubscribedToCaseNotification(USER_EMAIL, "MIXED-CASE-URN");
    }

    @Test
    void shouldReturnOkAndNotSendNotificationWhenAuthHeaderIsNull() {
        ResponseEntity<?> response = controller.notificationToCase(CASE_URN, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldReturnOkAndNotSendNotificationWhenAuthHeaderIsEmpty() {
        ResponseEntity<?> response = controller.notificationToCase(CASE_URN, "");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldReturnOkAndNotSendNotificationWhenAuthHeaderIsNotBasic() {
        ResponseEntity<?> response = controller.notificationToCase(CASE_URN, "Bearer some-jwt-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService, never()).sendUserSubscribedToCaseNotification(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString()
        );
    }
}
