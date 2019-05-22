/* INSERT LICENSE HERE */

package com.regolit.jscreader.util;

import java.util.ArrayList;
import java.lang.StringBuilder;

public class Util {
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
}