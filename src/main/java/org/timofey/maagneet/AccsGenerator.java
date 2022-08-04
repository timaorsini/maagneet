package org.timofey.maagneet;

import org.timofey.sms_activate.error.base.SMSActivateBaseException;

import java.io.IOException;

public class AccsGenerator {

    /**
     * args[0] should be API key for VAK sms service
     */
    public static void main(String[] args) throws InterruptedException, SMSActivateBaseException, IOException {

        for (int i = 0; i < 1000; i++) {
            new AccGenerator(args[0]).generate(true);
        }
    }
}
