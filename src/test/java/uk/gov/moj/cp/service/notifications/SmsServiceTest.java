package uk.gov.moj.cp.service.notifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendSmsResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private SendSmsResponse sendSmsResponse;

    @Mock
    private Notification notification;

    @InjectMocks
    private SmsService smsService;

    private static final String TARGET_MOBILE = "+447700900000";
    private static final String TEMPLATE_ID = "sms-template-123";
    private static final String REFERENCE = "ref-1";
    private static final String NOTIFICATION_ID = "abc-123";

    @Test
    void shouldDelegateToNotificationClientForSendSms() throws NotificationClientException {
        Map<String, String> params = Map.of("caseurn", "URN1", "email", TARGET_MOBILE);
        when(notificationClient.sendSms(TEMPLATE_ID, TARGET_MOBILE, params, REFERENCE))
            .thenReturn(sendSmsResponse);

        SendSmsResponse actual = smsService.sendTextMessage(TARGET_MOBILE, TEMPLATE_ID, params, REFERENCE);

        assertThat(actual).isSameAs(sendSmsResponse);
        verify(notificationClient).sendSms(TEMPLATE_ID, TARGET_MOBILE, params, REFERENCE);
    }

    @Test
    void shouldPassArgumentsInCorrectOrderToNotificationClient() throws NotificationClientException {
        Map<String, String> params = Map.of("k", "v");
        when(notificationClient.sendSms(anyString(), anyString(), any(), any())).thenReturn(sendSmsResponse);

        smsService.sendTextMessage(TARGET_MOBILE, TEMPLATE_ID, params, REFERENCE);

        verify(notificationClient).sendSms(TEMPLATE_ID, TARGET_MOBILE, params, REFERENCE);
    }

    @Test
    void shouldAllowNullReferenceOnSendSms() throws NotificationClientException {
        Map<String, String> params = Map.of();
        when(notificationClient.sendSms(TEMPLATE_ID, TARGET_MOBILE, params, null))
            .thenReturn(sendSmsResponse);

        SendSmsResponse actual = smsService.sendTextMessage(TARGET_MOBILE, TEMPLATE_ID, params, null);

        assertThat(actual).isSameAs(sendSmsResponse);
        verify(notificationClient).sendSms(TEMPLATE_ID, TARGET_MOBILE, params, null);
    }

    @Test
    void shouldAllowEmptyParametersMapOnSendSms() throws NotificationClientException {
        Map<String, String> params = Map.of();
        when(notificationClient.sendSms(TEMPLATE_ID, TARGET_MOBILE, params, REFERENCE))
            .thenReturn(sendSmsResponse);

        smsService.sendTextMessage(TARGET_MOBILE, TEMPLATE_ID, params, REFERENCE);

        verify(notificationClient).sendSms(TEMPLATE_ID, TARGET_MOBILE, params, REFERENCE);
    }

    @Test
    void shouldPropagateNotificationClientExceptionFromSendSms() throws NotificationClientException {
        doThrow(new NotificationClientException("Gov Notify SMS down"))
            .when(notificationClient).sendSms(anyString(), anyString(), any(), any());

        assertThatThrownBy(() ->
            smsService.sendTextMessage(TARGET_MOBILE, TEMPLATE_ID, Map.of(), REFERENCE))
            .isInstanceOf(NotificationClientException.class)
            .hasMessageContaining("Gov Notify SMS down");
    }

    @Test
    void shouldDelegateToNotificationClientForGetNotificationById() throws NotificationClientException {
        when(notificationClient.getNotificationById(NOTIFICATION_ID)).thenReturn(notification);

        Notification actual = smsService.getNotificationById(NOTIFICATION_ID);

        assertThat(actual).isSameAs(notification);
        verify(notificationClient).getNotificationById(NOTIFICATION_ID);
    }

    @Test
    void shouldPropagateNotificationClientExceptionFromGetNotificationById() throws NotificationClientException {
        doThrow(new NotificationClientException("Notification not found"))
            .when(notificationClient).getNotificationById(anyString());

        assertThatThrownBy(() -> smsService.getNotificationById(NOTIFICATION_ID))
            .isInstanceOf(NotificationClientException.class)
            .hasMessageContaining("Notification not found");
    }
}
