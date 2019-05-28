/* INSERT LICENSE HERE */

package com.regolit.jscreader.util;

import java.util.List;
import java.util.ArrayList;
import java.lang.StringBuilder;
import java.util.Map;
import java.util.HashMap;


public class Util {
    public static class BreakException extends Exception {}

    /**
     * Convert byte array to HEX representation: "XX XX XX XX ..."
     * 
     * @param  bytes [description]
     * @return       [description]
     */
    public static String hexify(byte[] bytes) {
        ArrayList<String> bytesStrings = new ArrayList<String>(bytes.length);
        for (byte b : bytes) {
            bytesStrings.add(String.format("%02X", b));
        }
        return String.join(" ", bytesStrings);
    }

    /**
     * Convert byte array to HEX representation: "XX XX XX XX ..." wrapped at position "wrap"
     * 
     * @param  bytes [description]
     * @param  int  
     * @return       [description]
     */
    public static String hexify(byte[] bytes, int wrap) {
        ArrayList<String> bytesStrings = new ArrayList<String>(bytes.length);
        int i = 1;
        for (byte b : bytes) {
            if ((i % wrap) == 0) {
                bytesStrings.add(String.format("%02X\n", b));
            } else {
                bytesStrings.add(String.format("%02X", b));
            }
            i++;
        }
        return String.join(" ", bytesStrings);
    }

    public static String bytesToString(byte[] bytes) {
        try {
            return new String(bytes, "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {
            return "";
        }
    }

    public static byte[] concatArrays(byte[] a, byte[] b) {
        byte[] buffer = new byte[a.length + b.length];
        System.arraycopy(a, 0, buffer, 0, a.length);
        System.arraycopy(b, 0, buffer, a.length, b.length);
        return buffer;
    }

    public static byte[] copyArray(byte[] buffer, int from, int length) {
        byte[] res = new byte[length];
        System.arraycopy(buffer, from, res, 0, length);
        return res;
    }

    public static String asciify(byte[] bytes) {
        // var characters = new ArrayList<Character>(bytes.length);
        StringBuilder sb = new StringBuilder(bytes.length); 
        for (var b : bytes) {
            if (b >= 32 && b <= 126) {
                sb.append(Character.valueOf((char)b));
            } else {
                sb.append('.');
            }
        }
        return sb.toString();
    }

    public static byte[] toByteArray(String s) {
        int len = s.length();
        byte[] buf = new byte[len/2];
        int bufLen = 0;
        int i = 0;
        
        while (i < len) {
            char c1 = s.charAt(i);
            i++;
            if (c1 == ' ') {
                continue;
            }
            char c2 = s.charAt(i);
            i++;

            byte d = (byte)((Character.digit(c1, 16) << 4) + (Character.digit(c2, 16)));
            buf[bufLen] = d;
            ++bufLen;
        }

        return copyArray(buf, 0, bufLen);
    }

    public static int unsignedByte(byte b) {
        int i = b;
        if (i < 0) {
            i += 256;
        }
        return i;
    }

    public static void errorLog(String s) {
        System.err.println(s);
    }

    // Convert HCD (Hex Coded Decimal) byte to Decimal
    public static int hcdByteToInt(byte b) {
        int lb = b & 0xF;
        int hb = (b >> 4) & 0xF;
        return hb * 10 + lb;
    }
    
    // convert YYMMDD BCD to YYYY-MM-DD
    public static String bytesToDate(byte[] bytes) {
        String res = "";
        return String.format("%04d-%02d-%02d", 2000+hcdByteToInt(bytes[0]), hcdByteToInt(bytes[1]), hcdByteToInt(bytes[2]));
    }

    public static Map<String, String> mapEmvDataObjects(List<BerTlv> objects) {
        HashMap<String, String> res = new HashMap<String, String>();

        for (BerTlv b : objects) {
            byte[] tagBytes = b.getTag();
            String tagString = hexify(tagBytes);

            // convert tag to int value
            int tag = 0;
            for (int i=0; i<tagBytes.length; i++) {
                int x = tagBytes[i];
                if (x < 0) {
                    x += 256;
                }
                tag = tag*256 + x;
            }

            String name = null;
            byte[] value = b.getValue();
            String displayValue = Util.hexify(value);

            switch (tag) {
            case 0x56:
                name = "Track 1 Data";
                break;
            case 0x57:
                name = "Track 2 Equivalent Data";
                break;
            case 0x5A:
                name = "Application Primary Account Number (PAN)";
                break;
            case 0x5F20:
                name = "Cardholder Name";
                displayValue = bytesToString(value);
                break;
            case 0x5F24:
                name = "Application Expiration Date";
                displayValue = bytesToDate(value);
                break;
            case 0x5F25:
                name = "Application Effective Date";
                displayValue = bytesToDate(value);
                break;
            case 0x5F28:
                name = "Issuer Country Code";
                // displayValue = bytesToString(value);
                break;
            case 0x5F30:
                name = "Service Code";
                break;
            case 0x5F34:
                name = "Application Primary Account Number (PAN) Sequence Number";
                break;
            case 0x8C:
                name = "Card Risk Management Data Object List 1 (CDOL1)";
                break;
            case 0x8D:
                name = "Card Risk Management Data Object List 2 (CDOL2)";
                break;
            case 0x8E:
                name = "Cardholder Verification Method (CVM) List";
                break;
            case 0x8F:
                name = "Certification Authority Public Key Index";
                break;
            case 0x90:
                name = "Issuer Public Key Certificate";
                break;
            case 0x92:
                name = "Issuer Public Key Remainder";
                break;
            case 0x93:
                name = "Signed Static Application Data";
                break;
            case 0x9F07:
                name = "Application Usage Control";
                break;
            case 0x9F08:
                name = "Application Version Number";
                break;
            case 0x9F0D:
                name = "Issuer Action Code - Default";
                break;
            case 0x9F0E:
                name = "Issuer Action Code - Denial";
                break;
            case 0x9F0F:
                name = "Issuer Action Code - Online";
                break;
            case 0x9F1F:
                name = "Track 1 Discretionary Data";
                break;
            case 0x9F32:
                name = "Issuer Public Key Exponent";
                break;
            case 0x9F42:
                name = "Application Currency Code";
                break;
            case 0x9F44:
                name = "Application Currency Exponent";
                break;
            case 0x9F46:
                name = "ICC Public Key Certificate";
                break;
            case 0x9F47:
                name = "ICC Public Key Exponent";
                break;
            case 0x9F48:
                name = "ICC Public Key Remainder";
                break;
            case 0x9F49:
                name = "Dynamic Data Authentication Data Object List (DDOL)";
                break;
            case 0x9F4A:
                name = "Static Data Authentication Tag List";
                break;
            case 0x9F62:
                name = "PCVC3 (Track1)";
                break;
            case 0x9F63:
                name = "PUNATC (Track1)";
                break;
            case 0x9F64:
                name = "NATC (Track1)";
                break;
            case 0x9F65:
                name = "PCVC3 (Track2)";
                break;
            case 0x9F66:
                name = "Terminal Transaction Qualifiers (TTQ)";
                break;
            case 0x9F67:
                name = "NATC (Track2)";
                break;
            case 0x9F68:
                name = "Card Additional Processes";
                break;
            case 0x9F6B:
                name = "Track 2 Data/Card CVM Limit";
                break;
            case 0x9F6C:
                name = "Card Transaction Qualifiers (CTQ)";
                break;
            default:
                name = hexify(tagBytes);
            }

            res.put(tagString, String.format("%s: %s", name, displayValue));
        }

        return res;
    }
}