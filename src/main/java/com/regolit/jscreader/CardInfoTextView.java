/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.util.Util;
import com.regolit.jscreader.util.ATR;
import com.regolit.jscreader.util.BerTlv;
import com.regolit.jscreader.model.*;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TextArea;
import javafx.beans.value.ObservableValue;
import java.lang.StringBuilder;

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
                } else if (nodeValue instanceof CardItemSFIModel) {
                    processValue((CardItemSFIModel)nodeValue);
                } else if (nodeValue instanceof CardItemAdfFCIModel) {
                    processValue((CardItemAdfFCIModel)nodeValue);
                } else if (nodeValue instanceof CardItemFCIModel) {
                    processValue((CardItemFCIModel)nodeValue);
                }
                // System.out.println(nodeValue.getClass().getName());
                // processValue(nodeValue);
            }
        });
    }

    private void processValue(CardItemGeneralInformationModel value) {
        processValue((CardItemRootModel)value);

        var sb = new StringBuilder(getText());
        sb.append(String.format("ATR: %s%n", Util.hexify(value.ATR)));
        var atr = new ATR(value.ATR);
        sb.append(atr.parseToText());
        sb.append("\n");
        setText(sb.toString());
    }

    private void processValue(CardItemSFIModel value) {
        processValue((CardItemRootModel)value);

        var sb = new StringBuilder(getText());
        sb.append("Raw EF data\n\n");
        sb.append(String.format("  Total records: %d\n", value.records.size()));

        var iter = value.records.listIterator();
        while (iter.hasNext()) {
            sb.append(String.format("    Record %d: %s\n", iter.nextIndex(), 
                Util.hexify(iter.next())));
        }
        setText(sb.toString());
    }

    private void processValue(CardItemFCIModel value) {
        processValue((CardItemRootModel)value);

        var sb = new StringBuilder(getText());
        try {
            var root = BerTlv.parseBytes(value.fciData);
            sb.append("Decoded BER-TLV data:\n");
            sb.append(root.toString());
            sb.append("\n");
        // } catch (BerTlv.ConstraintException e) {
        //     sb.append(String.format("Failed to parse FCI data: %s\n", e));
        } catch (BerTlv.ParsingException e) {
            sb.append(String.format("Failed to parse FCI data: %s\n", e));
        }
        setText(sb.toString());
    }

    private void processValue(CardItemAdfFCIModel value) {
        processValue((CardItemFCIModel)value);

        var sb = new StringBuilder(getText());
        sb.append(String.format("Application name: %s%n", value.name));
        setText(sb.toString());
    }

    private void processValue(CardItemRootModel value) {
        var text = "";
        text += value.title + "\n\n";
        setText(text);
    }
}