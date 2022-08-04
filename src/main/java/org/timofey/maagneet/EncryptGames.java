package org.timofey.maagneet;


import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Extracted (and partly rewritten) from an official Maagneet apk
*   Generates requests signature
*/
public final class EncryptGames {

    private static byte[] m55429a(byte[] bArr, String str) {
        try {
            byte[] bArr2;
            SecretKeySpec secretKeySpec = new SecretKeySpec(bArr, "HmacSHA512");
            Mac instance = Mac.getInstance("HmacSHA512");
            instance.init(secretKeySpec);
            if (str == null) {
                bArr2 = null;
            } else {
                bArr2 = str.getBytes(StandardCharsets.UTF_8);
            }
            return instance.doFinal(bArr2);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            System.err.println("Some shit with getting HMAC");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private static Object invoke(Object obj) {
        return String.format("%02x", Arrays.copyOf(new Object[]{((Number) obj).byteValue()}, 1));
    }

    /**
     * @return X-Request-Sign header
     */
    public static String generateSign(String appVersion, String deviceId, String phone, String urlPath) {
        byte[] bytes = ("6xC" + ")%!X" + "~^wso" + ":39_").getBytes(StandardCharsets.UTF_8);
        String lowerCase = "android";
        byte[] a = m55429a(bytes, lowerCase);
        Locale locale2 = Locale.getDefault();
        String lowerCase2 = appVersion.toLowerCase(locale2);
        byte[] a2 = m55429a(a, lowerCase2);
        Locale locale3 = Locale.getDefault();
        String lowerCase3 = deviceId.toLowerCase(locale3);
        byte[] a3 = m55429a(a2, lowerCase3);
        Locale locale4 = Locale.getDefault();
        String lowerCase4 = phone.toLowerCase(locale4);
        byte[] a4 = m55429a(a3, lowerCase4);
        Locale locale5 = Locale.getDefault();
        String lowerCase5 = urlPath.toLowerCase(locale5);
        byte[] a5 = m55429a(a4, lowerCase5);
        StringBuilder sb = new StringBuilder();
        for (byte b : a5) {
            sb.append((CharSequence) invoke(b));
        }
        return sb.toString();
    }

}

