package org.timofey.sms_activate.error;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.timofey.sms_activate.error.base.SMSActivateBaseException;

public class SMSActivateUnknownException extends SMSActivateBaseException {
  /**
   * Message unknown error
   */
  private final String messageUnknownError;

  /**
   * Constructor unknown sms activate exception with multi-lang.
   *
   * @param error name not documented error.
   * @param messageUnknownError message unknown error.
   */
  public SMSActivateUnknownException(@NotNull String error, @Nullable String messageUnknownError) {
    super("Send request developer.", "Обратитесь к разработчикам.");
    this.messageUnknownError = error + " " + (messageUnknownError == null ? "" : messageUnknownError);
  }

  /**
   * Returns the message unknown error.
   *
   * @return message unknown error.
   */
  @Nullable
  public String getMessageUnknownError() {
    return messageUnknownError;
  }

  @Override
  @NotNull
  public String getMessage() {
    String eng = super.getEnglishMessage() + " " + this.messageUnknownError;
    String rus = super.getRussianMessage() + " " + this.messageUnknownError;
    return String.join(" | ", eng, rus);
  }
}
