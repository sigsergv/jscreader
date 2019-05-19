/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.event.ChangeEvent;
import com.regolit.jscreader.event.CardInsertedListener;
import com.regolit.jscreader.event.CardRemovedListener;

import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import javafx.application.Platform;
import javax.smartcardio.CardException;

class CardItemsTree extends TreeView
    implements CardInsertedListener, CardRemovedListener
{
    public static class RootModel {
        String title;
        RootModel(String title) {
            this.title = title;
        }
        @Override public String toString() {
            return title;
        }
    }

    public static class CardGeneralInfoModel extends RootModel {
        byte[] ATR;
        CardGeneralInfoModel(String title) {
            super(title);
        }
    }

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
        var root = new TreeItem();
        root.setExpanded(true);
        setRoot(root);

        // "General information" node
        var cardGeneralInfo = new CardGeneralInfoModel("General Information");
        try {
            var card = terminal.connect("*");
            cardGeneralInfo.ATR = card.getATR().getBytes();

        } catch (CardException e) {
            System.err.printf("Card read failed: %s%n", e);
        }
        var generalInformationNode = new TreeItem(cardGeneralInfo);
        root.getChildren().addAll(generalInformationNode);

        // other nodes

        // Select "General Information" node
        getSelectionModel().selectFirst();
    }
}