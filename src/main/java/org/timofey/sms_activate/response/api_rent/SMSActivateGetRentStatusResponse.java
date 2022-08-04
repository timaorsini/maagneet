package org.timofey.sms_activate.response.api_rent;

import org.jetbrains.annotations.NotNull;
import org.timofey.sms_activate.response.api_rent.extra.SMSActivateSMS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SMSActivateGetRentStatusResponse {
  /**
   * Count sms.
   */
  private int quantity;

  /**
   * Service name.
   */
  private String service;

  /**
   * SMS list from server.
   */
  private Map<Integer, SMSActivateSMS> values;

  private SMSActivateGetRentStatusResponse() {
  }

  /**
   * Returns the count sms.
   *
   * @return count sms.
   */
  public int getCountSms() {
    return quantity;
  }

  /**
   * Returns the list sms from server.
   *
   * @return list sms from server.
   */
  @NotNull
  public List<SMSActivateSMS> getSmsActivateSMSList() {
    return new ArrayList<>(values.values());
  }

  /**
   * Returns the service name.
   *
   * @return service name
   */
  @NotNull
  public String getService() {
    return service;
  }
}
