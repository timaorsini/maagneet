package org.timofey.sms_activate;

import org.jetbrains.annotations.NotNull;
import org.timofey.sms_activate.response.api_rent.extra.SMSActivateRentActivation;

class SMSActivateGetRentNumberResponse {
  /**
   * Rent phone.
   */
  private SMSActivateRentActivation phone;

  /**
   * Returns the rent phone.
   *
   * @return rent phone.
   */
  @NotNull
  public SMSActivateRentActivation getSMSmsActivateGetRentNumber() {
    return phone;
  }
}
