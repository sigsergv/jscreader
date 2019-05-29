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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.stage.Stage;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import javafx.application.Platform;
import javax.smartcardio.CardException;
import javax.smartcardio.CardChannel;
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
        var progressWindow = Main.createProgressWindow("Reading card");
        progressWindow.show();

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

            var discoveredApps = new ArrayList<String>(5);


            // discover MF
            discoveredApps.addAll(processMF(channel, root));

            // try yubikey app
            discoveredApps.addAll(processYubikey(channel, root));

            // try OpenPGP application
            discoveredApps.addAll(processOpenpgp(channel, root));

            // try EMV PSE
            discoveredApps.addAll(processPSE1(channel, root));

            // walk through list of known applications
            var selectAppCommandTemplate = Util.toByteArray("00 A4 04 00");
            for (var x : CandidateApplications.getInstance().list()) {
                if (!x.getEnabled()) {
                    continue;
                }
                var aid = x.getAid();
                if (discoveredApps.indexOf(Util.hexify(aid)) != -1) {
                    continue;
                }
                byte[] cmdLcPart = {(byte)aid.length};
                cmd = Util.concatArrays(cmdLcPart, aid);
                cmd = Util.concatArrays(selectAppCommandTemplate, cmd);
                answer = channel.transmit(new CommandAPDU(cmd));
                if (answer.getSW() == 0x9000) {
                    // insert found ADF info
                    var adfInfo = new CardItemAdfFCIModel(String.format("ADF (AID=%s)", Util.hexify(aid)), 
                        answer.getData(), aid, x.getType(), x.getName());
                    var adfNode = new TreeItem<CardItemRootModel>(adfInfo);
                    root.getChildren().add(adfNode);
                }
            }

            // Select "General Information" node
            getSelectionModel().selectFirst();
        } catch (CardException e) {
            System.err.printf("Card read failed: %s%n", e);
            e.printStackTrace();
        }
        progressWindow.close();
    }

    private List<String> processMF(CardChannel channel, TreeItem<CardItemRootModel> parent)
        throws CardException
    {
        var discoveredApps = new ArrayList<String>();
        var cmd = Util.toByteArray("00 A4 00 00 02 3F 00 00");
        var answer = channel.transmit(new CommandAPDU(cmd));

        if (answer.getSW() == 0x9000) {
            // insert node with master DF information
            var info = new CardItemFCIModel("MF", answer.getData());
            var node = new TreeItem<CardItemRootModel>(info);
            parent.getChildren().add(node);

            // TODO: add EF.DIR, EF.ATR and other files
        } else {
            System.out.printf("No MF, SW: %04X\n", answer.getSW());
        }
        return discoveredApps;
    }

    private List<String> processYubikey(CardChannel channel, TreeItem<CardItemRootModel> parent)
        throws CardException
    {
        var discoveredApps = new ArrayList<String>();
        var cmd = Util.toByteArray("00 A4 04 00 07 A0 00 00 05 27 21 01");
        var answer = channel.transmit(new CommandAPDU(cmd));
        if (answer.getSW() == 0x9000) {
            System.out.printf("DATA: %s\n", Util.hexify(answer.getData()));
            discoveredApps.add("A0 00 00 05 27 21 01");
        }
        return discoveredApps;
    }

    private List<String> processOpenpgp(CardChannel channel, TreeItem<CardItemRootModel> parent)
        throws CardException
    {
        var discoveredApps = new ArrayList<String>();
        var cmd = Util.toByteArray("00 A4 04 00 06 D2 76 00 01 24 01");
        var answer = channel.transmit(new CommandAPDU(cmd));
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

            var info = new CardItemPGPModel("OpenPGP (AID=D2 76 00 01 24 01)", dataObjects);
            var node = new TreeItem<CardItemRootModel>(info);
            // node.setExpanded(true);
            parent.getChildren().add(node);
            discoveredApps.add("D2 76 00 01 24 01");
        }
        return discoveredApps;
    }

    private List<String> processPSE1(CardChannel channel, TreeItem<CardItemRootModel> parent)
        throws CardException
    {
        var discoveredApps = new ArrayList<String>();
        var cmd = Util.toByteArray("00 A4 04 00 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31");
        var answer = channel.transmit(new CommandAPDU(cmd));
        if (answer.getSW() == 0x9000) {
            var pseFciData = answer.getData();
            var pseInfo = new CardItemPSE1FCIModel("EMV: 1PAY.SYS.DDF01", pseFciData);
            var pseNode = new TreeItem<CardItemRootModel>(pseInfo);
            pseNode.setExpanded(true);
            parent.getChildren().add(pseNode);

            try {
                BerTlv tlvRoot = BerTlv.parseBytes(pseFciData);

                // find EF with application data
                var piTlv = tlvRoot.getPart("A5");
                if (piTlv != null) {
                    // piTlv now contains data specified in EMV_v4.3 book 1 spec,
                    // section "11.3.4 Data Field Returned in the Response Message"
                    var sfiTlv = piTlv.getPart("88");
                    if (sfiTlv != null) {
                        var defSfiData = sfiTlv.getValue();
                        int sfi = defSfiData[0];
                        List<byte[]> sfiRecords = APDU.sfiRecords(channel, sfi);
                        var sfiInfo = new CardItemSFIModel(String.format("SFI=%d", sfi), sfiRecords);
                        var sfiNode = new TreeItem<CardItemRootModel>(sfiInfo);
                        pseNode.getChildren().add(sfiNode);

                        // now analyze sfiRecords and find installed payment applications
                        for (var record : sfiRecords) {
                            BerTlv psd = BerTlv.parseBytes(record);
                            // psd must have tag "70"
                            // see EMV_v4.3 book 1, section "12.2.3 Coding of a Payment System Directory"
                            if (!psd.tagEquals("70")) {
                                continue;
                            }
                            for (var p : psd.getParts()) {
                                if (!p.tagEquals("61")) {
                                    continue;
                                }
                                BerTlv aidTlv = p.getPart("4F");
                                var aid = aidTlv.getValue();
                                discoveredApps.addAll(processEmvAid(channel, parent, aid));
                            }
                        }
                    }
                }
            } catch (BerTlv.ParsingException e) {
                Util.errorLog("Failed to parse data while processing PSE1");
            } catch (BerTlv.ConstraintException e) {
                Util.errorLog("Failed to parse (constraint) data while processing PSE1");
            }
            discoveredApps.add("31 50 41 59 2E 53 59 53 2E 44 44 46 30 31");
        }
        return discoveredApps;
    }

    private List<String> processEmvAid(CardChannel channel, TreeItem<CardItemRootModel> parent, byte[] aid)
        throws CardException
    {
        var discoveredApps = new ArrayList<String>();
        var selectAppCommandTemplate = Util.toByteArray("00 A4 04 00");
        byte[] cmdLcPart = {(byte)aid.length};
        var cmd = Util.concatArrays(cmdLcPart, aid);
        cmd = Util.concatArrays(selectAppCommandTemplate, cmd);
        var answer = channel.transmit(new CommandAPDU(cmd));
        if (answer.getSW() == 0x9000) {
            var aidString = Util.hexify(aid);
            var appFciData = answer.getData();

            // add child EFs
            try {
                var tlvRoot = BerTlv.parseBytes(appFciData);
                var piTlv = tlvRoot.getPart("A5");
                if (piTlv != null) {
                    // start financial transaction
                    byte[] pdolData = null;
                    var pdolTlv = piTlv.getPart("9F 38");
                    if (pdolTlv != null) {
                        pdolData = pdolTlv.getValue();
                    }

                    var dolData = Util.toByteArray("83 00");
                    if (pdolData != null) {
                        // parse pdol data and extract total fields length
                        // ignore tags
                        boolean lengthByte = false;
                        int totalLength = 0;
                        for (var b : pdolData) {
                            if (lengthByte) {
                                int x = Util.unsignedByte(b);
                                totalLength += x;
                                lengthByte = false;
                                continue;
                            }
                            if ((b & 0x1F) != 0x1F) {
                                // ^^^^^^ last five bits of "b" are not all 1s, so this byte is last one
                                // in tag block, so consider next byte as field length
                                lengthByte = true;
                            }
                        }
                        var t = new byte[totalLength];
                        dolData[1] = (byte)totalLength;  // remember, dolData = "83 00"
                        dolData = Util.concatArrays(dolData, t);
                    }

                    // Send command "GET PROCESSING OPTIONS"
                    var gpoCommand = Util.toByteArray("80 A8 00 00 00");
                    gpoCommand[4] = (byte)dolData.length;
                    gpoCommand = Util.concatArrays(gpoCommand, dolData);
                    gpoCommand = Util.concatArrays(gpoCommand, Util.toByteArray("00"));
                    answer = channel.transmit(new CommandAPDU(gpoCommand));
                    if (answer.getSW() != 0x9000) {
                        throw new Util.BreakException();
                    }
                    var data = answer.getData();
                    byte[] aipData = null;
                    byte[] aflData = null;  
                    var gpoTlv = BerTlv.parseBytes(data);
                    if (gpoTlv.tagEquals("77")) {
                        aipData = gpoTlv.getPart("82").getValue();
                        aflData = gpoTlv.getPart("94").getValue();
                    } else if (gpoTlv.tagEquals("80")) {
                        byte[] gpoData = gpoTlv.getValue();
                        aipData = Util.copyArray(gpoData, 0, 2);
                        aflData = Util.copyArray(gpoData, 2, gpoData.length-2);
                    } else {
                        Util.errorLog("EMV ADF parse failed: GPO");
                        throw new Util.BreakException();
                    }

                    // Finally add app node
                    var info = new CardItemEmvFCIModel(String.format("EMV: AID=%s", aidString), appFciData, aipData, aflData);
                    var node = new TreeItem<CardItemRootModel>(info);
                    node.setExpanded(true);
                    parent.getChildren().add(node);

                    // find EFs
                    int aflPartsCount = aflData.length / 4;
                    var readRecordCommand = Util.toByteArray("00  B2  00 00 00");
                    for (int i=0; i<aflPartsCount; i++) {
                        int startByte = i*4;
                        byte sfi = (byte)(aflData[startByte] >> 3);
                        byte firstSfiRec = aflData[startByte + 1];
                        byte lastSfiRec = aflData[startByte + 2];

                        // List<byte[]> sfiRecords = APDU.sfiRecords(channel, sfi);
                        List<byte[]> sfiRecords = APDU.sfiRecords(channel, sfi, firstSfiRec, lastSfiRec);
                        var sfiInfo = new CardItemEmvSFIModel(String.format("SFI=%d", sfi), sfiRecords);
                        var sfiNode = new TreeItem<CardItemRootModel>(sfiInfo);
                        node.getChildren().add(sfiNode);
                    }
                    discoveredApps.add(aidString);
                }

            } catch (Util.BreakException e) {
                // do nothing
            } catch (BerTlv.ParsingException e) {
                Util.errorLog("Failed to parse data while processing PSE1");
            } catch (BerTlv.ConstraintException e) {
                Util.errorLog("Failed to parse (constraint) data while processing PSE1");
            }
        }
        return discoveredApps;
    }
}