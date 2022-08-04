package org.timofey.sms_activate.response.api_activation;

import org.jetbrains.annotations.NotNull;
import org.timofey.sms_activate.response.api_activation.enums.SMSActivateStatusNumber;

public class SMSActivateGetFullSmsResponse {
  /**
   * Sms text.
   */
  private final String text;

  /**
   * Activation status.
   */
  private final SMSActivateStatusNumber smsActivateStatusNumber;

  /**
   * Constructor response getFullSMS with data from server.
   *
   * @param text                    sms text.
   * @param smsActivateStatusNumber activation status.
   */
  public SMSActivateGetFullSmsResponse(@NotNull SMSActivateStatusNumber smsActivateStatusNumber, @NotNull String text) {
    this.smsActivateStatusNumber = smsActivateStatusNumber;
    this.text = text;
  }

  /**
   * Returns the status activation.
   *
   * @return status activation.
   */
  @NotNull
  public SMSActivateStatusNumber getSmsActivateGetFullTypeResponse() {
    return smsActivateStatusNumber;
  }

  /**
   * Returns the text sms.
   *
   * @return text sms.
   */
  @NotNull
  public String getText() {
    return text;
  }
}
