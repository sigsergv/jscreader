/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TextArea;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

class CardInfoTextView extends TextArea {
    private CardItemsTree cardInfoTree;

    public CardInfoTextView(CardItemsTree cardInfoTree) {
        this.cardInfoTree = cardInfoTree;
        setEditable(false);

        // subscribe
        this.cardInfoTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue == null) {
                    // clear output?
                    return;
                }

                var nodeValue = ((TreeItem)newValue).getValue();
                if (nodeValue instanceof CardItemsTree.CardGeneralInfoModel) {
                    processValue((CardItemsTree.CardGeneralInfoModel)nodeValue);
                }
                // System.out.println(nodeValue.getClass().getName());
                // processValue(nodeValue);
            }
        });
    }

    private void processValue(CardItemsTree.CardGeneralInfoModel value) {
        processValue((CardItemsTree.RootModel)value);

        var text = getText();
        text += String.format("ATR: %s%n", Util.hexify(value.ATR));
        setText(text);
    }

    private void processValue(CardItemsTree.RootModel value) {
        var text = "";
        text += value.title + "\n\n";
        setText(text);
    }
}