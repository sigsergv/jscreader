/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import java.util.ArrayList;

class Util {
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

}