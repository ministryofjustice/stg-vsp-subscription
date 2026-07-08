package uk.gov.moj.cp.service.notifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NotificationService notificationService;

    private static final String TARGET_EMAIL = "target@email";
    private static final String SERVICE_URL = "https://example.com";
    private static final String TEMPLATE_ID = "template-123";
    private static final String USER_EMAIL = "user@example.com";
    private static final String CASE_URN = "URN123";

    @BeforeEach
    void setUp() {
        // lenient stubbing only when needed — set inside each test
    }

    @Test
    void shouldSendEmailWithExpectedParameters() throws NotificationClientException {
        when(notificationsProperties.getTargetEmail()).thenReturn(TARGET_EMAIL);
        when(notificationsProperties.getServiceUrl()).thenReturn(SERVICE_URL);
        when(notificationsProperties.getUserLoggedInEmailTemplateId()).thenReturn(TEMPLATE_ID);

        notificationService.sendUserSubscribedToCaseNotification(USER_EMAIL, CASE_URN);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendMail(eq(TARGET_EMAIL), eq(TEMPLATE_ID), propsCaptor.capture(), eq(null));

        Map<String, String> props = propsCaptor.getValue();
        assertThat(props)
            .containsEntry("email", USER_EMAIL)
            .containsEntry("caseurn", CASE_URN)
            .containsEntry("serviceUrl", SERVICE_URL);
    }

    @Test
    void shouldNotSendEmailWhenUserEmailIsNull() {
        notificationService.sendUserSubscribedToCaseNotification(null, CASE_URN);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldNotSendEmailWhenUserEmailIsEmpty() {
        notificationService.sendUserSubscribedToCaseNotification("", CASE_URN);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldNotSendEmailWhenCaseUrnIsNull() {
        notificationService.sendUserSubscribedToCaseNotification(USER_EMAIL, null);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldNotSendEmailWhenCaseUrnIsEmpty() {
        notificationService.sendUserSubscribedToCaseNotification(USER_EMAIL, "");
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldPropagateNotificationClientException() throws NotificationClientException {
        when(notificationsProperties.getTargetEmail()).thenReturn(TARGET_EMAIL);
        when(notificationsProperties.getServiceUrl()).thenReturn(SERVICE_URL);
        when(notificationsProperties.getUserLoggedInEmailTemplateId()).thenReturn(TEMPLATE_ID);
        doThrow(new NotificationClientException("Gov Notify down"))
            .when(emailService).sendMail(any(), any(), any(), any());

        assertThatThrownBy(() ->
            notificationService.sendUserSubscribedToCaseNotification(USER_EMAIL, CASE_URN))
            .isInstanceOf(NotificationClientException.class)
            .hasMessageContaining("Gov Notify down");
    }

    @Test
    void shouldBuildPropertiesMapWithAllThreeKeys() {
        Map<String, String> props = notificationService.addProperties(USER_EMAIL, CASE_URN, SERVICE_URL);

        assertThat(props)
            .hasSize(3)
            .containsEntry("email", USER_EMAIL)
            .containsEntry("caseurn", CASE_URN)
            .containsEntry("serviceUrl", SERVICE_URL);
    }
}
