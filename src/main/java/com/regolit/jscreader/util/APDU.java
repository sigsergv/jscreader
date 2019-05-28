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

    /**
     * Read binary records from EF using its SFI. Parent DF must be selected in the
     * channel before call.
     */
    public static List<byte[]> sfiRecords(CardChannel channel, int sfi) {
        var records = new ArrayList<byte[]>();
        // read all records from this file
        // READ RECORD, see ISO/IEC 7816-4, section "7.3.3 READ RECORD (S) command"
        //                          CLA INS P1 P2  Le
        var cmd = Util.toByteArray("00  B2  00 00  00");
        // read single record specified in P1 from EF with short EF identifier sfi
        byte p2 = (byte)((sfi << 3) | 4);
        cmd[3] = p2;
        byte recordNumber = 1;
        byte expectedLength = 0;

        while (true) {
            cmd[2] = recordNumber;
            cmd[4] = expectedLength;
            try {
                var answer = channel.transmit(new CommandAPDU(cmd));
                if (answer.getSW1() == 0x6C) {
                    expectedLength = (byte)answer.getSW2();
                    continue;
                }
                if (answer.getSW() != 0x9000) {
                    break;
                }
                records.add(answer.getData());
                recordNumber++;
            } catch (CardException e) {
                break;
            }
            expectedLength = 0;
        }
        return records;
    }
    public static List<byte[]> sfiRecords(CardChannel channel, int sfi, byte firstRec, byte lastRec) {
        var records = new ArrayList<byte[]>();
        // read all records from this file
        // READ RECORD, see ISO/IEC 7816-4, section "7.3.3 READ RECORD (S) command"
        //                          CLA INS P1 P2  Le
        var cmd = Util.toByteArray("00  B2  00 00  00");
        // read single record specified in P1 from EF with short EF identifier sfi
        byte p2 = (byte)((sfi << 3) | 4);
        cmd[3] = p2;
        byte expectedLength = 0;

        for (byte recordNumber=firstRec; recordNumber<=lastRec; recordNumber++) {
            cmd[2] = recordNumber;
            cmd[4] = expectedLength;
            try {
                var answer = channel.transmit(new CommandAPDU(cmd));
                if (answer.getSW1() == 0x6C) {
                    expectedLength = (byte)answer.getSW2();
                    continue;
                }
                if (answer.getSW() != 0x9000) {
                    break;
                }
                records.add(answer.getData());
                recordNumber++;
            } catch (CardException e) {
                break;
            }
            expectedLength = 0;
        }
        return records;
    }
    
}