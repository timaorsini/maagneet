package org.timofey.sms_activate.error;

import org.jetbrains.annotations.NotNull;
import org.timofey.sms_activate.error.base.SMSActivateBaseException;

public class SMSActivateBannedException extends SMSActivateBaseException {
  /**
   * End date your bane.
   */
  private final String endDate;

  /**
   * Constructor sms activate banned exception with multi-lang, endDate.
   *
   * @param englishMessage message on english language.
   * @param russianMessage message on russian language.
   * @param endDate date to end ban.
   */
  public SMSActivateBannedException(@NotNull String englishMessage, @NotNull String russianMessage, @NotNull String endDate) {
    super(englishMessage, russianMessage);
    this.endDate = endDate;
  }

  /**
   * Returns the end date of bane.
   *
   * @return end date of bane
   */
  @NotNull
  public String getEndDate() {
    return endDate;
  }

  /**
   * Returns the concatenation messages with date.
   *
   * @return concatenation messages with date
   */
  @Override
  @NotNull
  public String getMessage() {
    return String.format("%s: %s./%s: %s.", super.getEnglishMessage(), endDate, super.getRussianMessage(), endDate);
  }
}
