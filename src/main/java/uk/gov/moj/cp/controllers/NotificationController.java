package uk.gov.moj.cp.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.moj.cp.config.ApiPaths;
import uk.gov.moj.cp.service.notifications.NotificationService;
import uk.gov.moj.cp.util.ApiUtils;

import java.util.Base64;

import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping(ApiPaths.PATH_API_NOTIFICATIONS)
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{case_urn}")
    public ResponseEntity<?> notificationToCase(
        @PathVariable("case_urn") String caseUrn,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String fullAuthorizationHeader) {

        final String caseUrnUpperCase = caseUrn.toUpperCase();
        log.atInfo().log("Received subscription request for caseUrn: {}", caseUrnUpperCase);

        if (StringUtils.isNotEmpty(fullAuthorizationHeader)) {
            if (fullAuthorizationHeader.startsWith(ApiUtils.BASIC_TOKEN_PREFIX)) {
                final String userEncodedEmail = fullAuthorizationHeader.substring(ApiUtils.BASIC_TOKEN_PREFIX.length());
                final String userEmail = new String(Base64.getDecoder().decode(userEncodedEmail));
                notificationService.sendUserSubscribedToCaseNotification(userEmail, caseUrnUpperCase);
            }
        }


        return ok().build();
    }
}
