/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.util.Util;
import com.regolit.jscreader.util.CandidateApplications;
import com.regolit.jscreader.event.ChangeEvent;
import com.regolit.jscreader.event.CardInsertedListener;
import com.regolit.jscreader.event.CardRemovedListener;

import com.regolit.jscreader.model.CardItemRootModel;
import com.regolit.jscreader.model.CardItemAdfModel;
import com.regolit.jscreader.model.CardItemGeneralInformationModel;

import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import javafx.application.Platform;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;

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
            var cardGeneralInfo = new CardItemGeneralInformationModel("General Information");
            cardGeneralInfo.ATR = card.getATR().getBytes();

            var generalInformationNode = new TreeItem<CardItemRootModel>(cardGeneralInfo);
            root.getChildren().add(generalInformationNode);

            // other nodes
            var channel = card.getBasicChannel();

            // try to select MF
            // P1=0 "select MF, DF or EF"
            // P2=0 "return FCI template"
            //                                         CLA INS P1 P2 Lc 
            byte[] selectMFCommand = Util.toByteArray("00  A4  00 00 02  3F 00");
            var answer = channel.transmit(new CommandAPDU(selectMFCommand));
            if (answer.getSW() == 0x9000) {
                // insert node with master DF information
            }

            // walk through list of known apps
            var cap = CandidateApplications.getInstance();
            //                                                  CLA INS P1 P2
            byte[] selectAppCommandTemplate = Util.toByteArray("00  A4  04 00");
            for (var x : cap.list()) {
                byte[] cmdLcPart = {(byte)x.aid.length};
                var cmdAidPart = Util.concatArrays(cmdLcPart, x.aid);
                var cmd = Util.concatArrays(selectAppCommandTemplate, cmdAidPart);
                answer = channel.transmit(new CommandAPDU(cmd));
                if (answer.getSW() == 0x9000) {
                    // insert found ADF info
                    var adfInfo = new CardItemAdfModel(String.format("ADF (AID=%s)", Util.hexify(x.aid)), x.aid, x.type, x.name);
                    var tn = new TreeItem<CardItemRootModel>(adfInfo);
                    root.getChildren().add(tn);
                    // System.out.printf("FOUND: %s\n", Util.hexify(cmd));
                }
            }

            // Select "General Information" node
            getSelectionModel().selectFirst();
        } catch (CardException e) {
            System.err.printf("Card read failed: %s%n", e);
        }
    }
}