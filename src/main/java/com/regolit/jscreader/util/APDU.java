/* INSERT LICENSE HERE */

package com.regolit.jscreader.util;

import java.util.List;
import java.util.ArrayList;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.CardException;

/**
 * This class incapsulates various commands (instructions).
 */
public class APDU {

    public static byte[] readOpenPgpDataObject(CardChannel channel, String tag) {
        var ps = Util.toByteArray(tag);
        byte p1 = 0;
        byte p2 = 0;

        if (ps.length == 1) {
            p2 = ps[0];
        } else if (ps.length == 2) {
            p1 = ps[0];
            p2 = ps[1];
        }

        //                                   P1 P2
        byte[] cmd = Util.toByteArray("00 CA 00 00 00");
        cmd[2] = p1;
        cmd[3] = p2;

        try {
            var answer = channel.transmit(new CommandAPDU(cmd));
            if (answer.getSW() != 0x9000) {
                return null;
            } else {
                return answer.getData();
            }
        } catch (CardException e) {
            System.err.printf("Failed to fetch dataObject for PGP card, tag=\"%s\"", tag);
            return null;
        }
    }
}