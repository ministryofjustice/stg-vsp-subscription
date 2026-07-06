package uk.gov.moj.cp.service.notifications;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.SendSmsResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;
    private final NotificationsProperties notificationProperties;

    private static final String USER_EMAIL = "email";
    private static final String CASE_URN = "caseurn";
    private static final String SERVICE_URL = "serviceUrl";

    @SneakyThrows
    public void sendUserSubscribedToCaseNotification(final String userEmail, final String caseUrn) {
        if (StringUtils.isNoneEmpty(userEmail) && StringUtils.isNoneEmpty(caseUrn)) {
            final String sentToEmail = notificationProperties.getTargetEmail();
            final String serviceUrl = notificationProperties.getServiceUrl();
            emailService.sendMail(
                    sentToEmail,
                    notificationProperties.getUserLoggedInEmailTemplateId(),
                    addProperties(userEmail, caseUrn, serviceUrl),
                    null
            );
            log.info("A user {} subscribed to case notifications for case {}", userEmail, caseUrn);
        }
    }

    @SneakyThrows
    public void sendTextMessageForTheCaseNotification(final String caseUrn) {
        final String targetMobile = notificationProperties.getTargetMobile();
        final String serviceUrl = notificationProperties.getServiceUrl();
        SendSmsResponse sendSmsResponse = null;

        if (StringUtils.isNoneEmpty(targetMobile) && StringUtils.isNoneEmpty(caseUrn)) {
            try {
                sendSmsResponse = smsService.sendTextMessage(
                        targetMobile,
                        notificationProperties.getTextMessageTemplateId(),
                        addProperties(targetMobile, caseUrn, serviceUrl),
                        null
                );
                log.info("A user {} searched for the case {}", targetMobile, caseUrn);
            } catch (Exception e) {
                log.error("A user {} searched for the case {}", targetMobile, caseUrn);
            }
        }

        try {
            Notification notificationStatus = smsService.getNotificationById(sendSmsResponse.getNotificationId().toString());
            log.info("A user {} searched for the case {} and delivery status {}", targetMobile, caseUrn, notificationStatus.getStatus());
        } catch (Exception e) {
            log.error("A user {} searched for the case {}", targetMobile, caseUrn);
        }
    }

    public Map<String, String> addProperties(final String userEmail, final String caseUrn, final String serviceUrl) {
        Map<String, String> customProps = new HashMap<>();
        customProps.put(USER_EMAIL, userEmail);
        customProps.put(CASE_URN, caseUrn);
        customProps.put(SERVICE_URL, serviceUrl);
        return customProps;
    }
}
