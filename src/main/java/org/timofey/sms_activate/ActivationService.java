package org.timofey.sms_activate;

/**
 * Enum with activation services supporting sms-activate API
 */
public enum ActivationService {

    VAKSMS("https://vak-sms.com/stubs/handler_api.php?");

    private final String baseUrl;

    ActivationService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
