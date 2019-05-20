/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.model.CardItemRootModel;
import com.regolit.jscreader.model.CardItemGeneralInformationModel;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TextArea;
import javafx.beans.value.ObservableValue;

class CardInfoTextView extends TextArea {
    private CardItemsTree cardInfoTree;

    public CardInfoTextView(CardItemsTree cardInfoTree) {
        this.cardInfoTree = cardInfoTree;
        setEditable(false);

        // subscribe
        this.cardInfoTree.getSelectionModel().selectedItemProperty().addListener(new javafx.beans.value.ChangeListener<TreeItem>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem> observable, TreeItem oldValue, TreeItem newValue) {
                if (newValue == null) {
                    // clear output?
                    return;
                }

                var nodeValue = ((TreeItem)newValue).getValue();
                if (nodeValue instanceof CardItemGeneralInformationModel) {
                    processValue((CardItemGeneralInformationModel)nodeValue);
                }
                // System.out.println(nodeValue.getClass().getName());
                // processValue(nodeValue);
            }
        });
    }

    private void processValue(CardItemGeneralInformationModel value) {
        processValue((CardItemRootModel)value);

        var text = getText();
        text += String.format("ATR: %s%n", Util.hexify(value.ATR));
        var atr = new ATR(value.ATR);
        text += atr.parseToText() + "\n";
        setText(text);
    }

    private void processValue(CardItemRootModel value) {
        var text = "";
        text += value.title + "\n\n";
        setText(text);
    }
}