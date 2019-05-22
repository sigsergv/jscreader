/* INSERT LICENSE HERE */

package com.regolit.jscreader.util;

import java.util.List;
import java.util.ArrayList;


public class SimpleTlv {
    public static class ParsingException extends Exception {
        public ParsingException(String message) {
            super(message);
        }
    }

    // private byte[] bytes;
    private final int tag;
    private final byte[] value;

    private SimpleTlv(byte tag, byte[] value) {
        // we need to convert signed byte to unsigned
        this.tag = Util.unsignedByte(tag);
        this.value = value;
    }

    public int getTag() {
        return tag;
    }

    public byte[] getValue() {
        return value;
    }

    public static List<SimpleTlv> parseBytes(byte[] bytes)
        throws ParsingException
    {
        var res = new ArrayList<SimpleTlv>(5);

        try {
            int pos = 0;
            while (true) {
                var blockLength = bytes[pos+1];
                res.add(new SimpleTlv(bytes[pos], Util.copyArray(bytes, pos+2, blockLength)));
                pos += 2 + blockLength;
                if (pos == bytes.length) {
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParsingException("parse failed");
        }

        return res;
    }
}