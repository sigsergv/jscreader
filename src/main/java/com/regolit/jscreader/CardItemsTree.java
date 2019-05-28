/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.util.APDU;
import com.regolit.jscreader.util.Util;
import com.regolit.jscreader.util.BerTlv;
import com.regolit.jscreader.util.CandidateApplications;
import com.regolit.jscreader.event.ChangeEvent;
import com.regolit.jscreader.event.CardInsertedListener;
import com.regolit.jscreader.event.CardRemovedListener;

import com.regolit.jscreader.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import javafx.application.Platform;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;


class CardItemsTree extends TreeView<CardItemRootModel>
    implements CardInsertedListener, CardRemovedListener
{
    public CardItemsTree() {
        // subscribe to card inserted event
        var dm = DeviceManager.getInstance();
        dm.addListener(this);

        setShowRoot(false);
    }

    public void cardInserted(ChangeEvent e) {
        Platform.runLater(this::readSelectedTerminalCard);
    }

    public void cardRemoved(ChangeEvent e) {
        // RFU
    }

    protected void readSelectedTerminalCard() {
        var dm = DeviceManager.getInstance();
        var terminalName = dm.getSelectedTerminalName();
        var terminal = dm.getTerminal(terminalName);

        if (terminal == null) {
            return;
        }

        // set new tree nodes
        var root = new TreeItem<CardItemRootModel>();
        root.setExpanded(true);
        setRoot(root);

        // "General information" node
        try {
            var card = terminal.connect("*");
            var cardGeneralInfo = new CardItemGeneralInformationModel("General Information",
                card.getATR().getBytes());
            var generalInformationNode = new TreeItem<CardItemRootModel>(cardGeneralInfo);
            root.getChildren().add(generalInformationNode);

            // other nodes
            var channel = card.getBasicChannel();
            byte[] cmd; 
            ResponseAPDU answer;

            // try to get initially selected application name
            cmd = Util.toByteArray("00 CA 00 4F 00");
            answer = channel.transmit(new CommandAPDU(cmd));
            if (answer.getSW() == 0x9000) {
                System.out.printf("Found initially selected app, AID=%s\n", Util.hexify(answer.getData()));
            } else {
                System.out.printf("No isap, SW: %04X\n", answer.getSW());
            }

            // try to select MF
            cmd = Util.toByteArray("00 A4 00 00 02 3F 00 00");
            answer = channel.transmit(new CommandAPDU(cmd));
            if (answer.getSW() == 0x9000) {
                // insert node with master DF information
                var mfInfo = new CardItemFCIModel("MF", answer.getData());
                var mfNode = new TreeItem<CardItemRootModel>(mfInfo);
                root.getChildren().add(mfNode);
            } else {
                System.out.printf("No MF, SW: %04X\n", answer.getSW());
            }

            // try to select EF.DIR
            //                                         CLA INS P1 P2 Lc 
            cmd = Util.toByteArray("00  A4  00 00 02  2F 01 00");
            answer = channel.transmit(new CommandAPDU(cmd));
            if (answer.getSW() == 0x9000) {
                // insert node with master DF information
                var efInfo = new CardItemFCIModel("EF.DIR", answer.getData());
                var efNode = new TreeItem<CardItemRootModel>(efInfo);
                root.getChildren().add(efNode);
            } else {
                System.out.printf("No MF, SW: %04X\n", answer.getSW());
            }

            // // try to read data from initially selected file
            // byte[] getMFDataCommand = Util.toByteArray("00 CA 00 00 00");
            // answer = channel.transmit(new CommandAPDU(getMFDataCommand));
            // if (answer.getSW() == 0x9000) {
            //     System.out.printf("MF DATA: %s\n", Util.hexify(answer.getData()));
            // } else {
            //     System.out.printf("No MF Data, SW=0x%04x\n", answer.getSW());
            // }

            var discoveredApps = new ArrayList<byte[]>(5);

            // try yubikey app
            cmd = Util.toByteArray("00   A4   04 00  07 A0 00 00 05 27 21 01");
            answer = channel.transmit(new CommandAPDU(cmd));
            if (answer.getSW() == 0x9000) {
                System.out.printf("DATA: %s\n", Util.hexify(answer.getData()));
            }

            // try OpenPGP application
            cmd = Util.toByteArray("00   A4   04 00  06 D2 76 00 01 24 01");
            answer = channel.transmit(new CommandAPDU(cmd));
            if (answer.getSW() == 0x9000) {
                // insert node with openpgp app info
                var dataObjects = new HashMap<String, byte[]>(10);
                String[] tags = {"4F", "5F 52", "C4", "6E", "7F 74", "5E", "65", "5F 50", "7A"};
                for (var t : tags) {
                    byte[] d = APDU.readOpenPgpDataObject(channel, t);
                    if (d == null) {
                        continue;
                    }
                    dataObjects.put(t, d);
                }

                var adfInfo = new CardItemPGPModel("OpenPGP (AID=D2 76 00 01 24 01", dataObjects);
                var adfNode = new TreeItem<CardItemRootModel>(adfInfo);
                // adfNode.setExpanded(true);
                root.getChildren().add(adfNode);
            } else {
                // System.out.printf("SW: %04X", answer.getSW());
            }

            // try EMV objects

            // select PSE: "1PAY.SYS.DDF01"
            //                                          CLA  INS  P1 P2   Lc  Data
            cmd = Util.toByteArray("00   A4   04 00   0E  31 50 41 59 2E 53 59 53 2E 44 44 46 30 31");
            answer = channel.transmit(new CommandAPDU(cmd));
            if (answer.getSW() == 0x9000) {
                // add PSE node
                var pseFciData = answer.getData();
                var pseInfo = new CardItemFCIModel("EMV PSE: 1PAY.SYS.DDF01", pseFciData);
                var pseNode = new TreeItem<CardItemRootModel>(pseInfo);
                pseNode.setExpanded(true);

                try {
                    BerTlv tlvRoot = BerTlv.parseBytes(pseFciData);
                    root.getChildren().add(pseNode);

                    // find registered applications
                    var piTlv = tlvRoot.getPart("A5");
                    if (piTlv != null) {
                        // piTlv now contains data specified in EMV_v4.3 book 1 spec,
                        // section "11.3.4 Data Field Returned in the Response Message"
                        var sfiTlv = piTlv.getPart("88");
                        if (sfiTlv != null) {
                            var defSfiData = sfiTlv.getValue();
                            int sfi = defSfiData[0];

                            // TODO: move this code to separate method
                            var sfiRecords = new ArrayList<byte[]>();
                            // read all records from this file
                            // READ RECORD, see ISO/IEC 7816-4, section "7.3.3 READ RECORD (S) command"
                            //                                           CLA INS P1 P2  Le
                            cmd = Util.toByteArray("00  B2  00 00  00");
                            // read single record specified in P1 from EF with short EF identifier sfi
                            byte p2 = (byte)((sfi << 3) | 4);
                            cmd[3] = p2;
                            byte recordNumber = 1;
                            byte expectedLength = 0;
                            while (true) {
                                cmd[2] = recordNumber;
                                cmd[4] = expectedLength;
                                answer = channel.transmit(new CommandAPDU(cmd));
                                if (answer.getSW1() == 0x6C) {
                                    expectedLength = (byte)answer.getSW2();
                                    continue;
                                }
                                if (answer.getSW() != 0x9000) {
                                    break;
                                }
                                sfiRecords.add(answer.getData());
                                recordNumber++;
                            }

                            var sfiInfo = new CardItemSFIModel(String.format("SFI=%d", sfi), sfiRecords);
                            var sfiNode = new TreeItem<CardItemRootModel>(sfiInfo);
                            pseNode.getChildren().add(sfiNode);
                        }
                    }
                } catch (BerTlv.ParsingException e) {
                } catch (BerTlv.ConstraintException e) {
                }
            }

            // walk through list of known apps
            //                                                  CLA INS P1 P2
            var selectAppCommandTemplate = Util.toByteArray("00  A4  04 00");
            for (var x : CandidateApplications.getInstance().list()) {
                byte[] cmdLcPart = {(byte)x.aid.length};
                cmd = Util.concatArrays(cmdLcPart, x.aid);
                cmd = Util.concatArrays(selectAppCommandTemplate, cmd);
                answer = channel.transmit(new CommandAPDU(cmd));
                if (answer.getSW() == 0x9000) {
                    // insert found ADF info
                    var adfInfo = new CardItemAdfFCIModel(String.format("ADF (AID=%s)", Util.hexify(x.aid)), 
                        answer.getData(), x.aid, x.type, x.name);
                    var adfNode = new TreeItem<CardItemRootModel>(adfInfo);
                    root.getChildren().add(adfNode);
                }
            }

            // Select "General Information" node
            getSelectionModel().selectFirst();
        } catch (CardException e) {
            System.err.printf("Card read failed: %s%n", e);
        }
    }
}