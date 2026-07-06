package uk.gov.moj.cp.service.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendSmsResponse;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsService {
    private final NotificationClient notificationClient;

    public SendSmsResponse sendTextMessage(
            final String targetMobile,
            final String smsTemplate,
            final Map<String, String> parameters,
            final String reference
    ) throws NotificationClientException {
        return notificationClient.sendSms(smsTemplate, targetMobile, parameters, reference);
    }

    public Notification getNotificationById(final String notificationId) throws NotificationClientException {
        return notificationClient.getNotificationById(notificationId);
    }
}
