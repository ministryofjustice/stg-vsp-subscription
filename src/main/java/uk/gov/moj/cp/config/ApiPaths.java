package uk.gov.moj.cp.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiPaths {

    public static final String PATH_API = "/api";

    public static final String PATH_API_HEALTH = PATH_API + "/health";

    public static final String PATH_API_NOTIFICATIONS = PATH_API + "/notifications";
    public static final String PATH_EMAIL_API_NOTIFICATIONS = "/email";
    public static final String PATH_SMS_API_NOTIFICATIONS = "/sms";
}
