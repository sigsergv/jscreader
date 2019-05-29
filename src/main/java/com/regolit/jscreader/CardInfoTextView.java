/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.util.Util;
import com.regolit.jscreader.util.ATR;
import com.regolit.jscreader.util.BerTlv;
import com.regolit.jscreader.util.SimpleTlv;
import com.regolit.jscreader.model.*;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import javafx.scene.control.TreeItem;
// import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.beans.value.ObservableValue;
import java.lang.StringBuilder;

class CardInfoTextView extends VBox {
    private CardItemsTree cardInfoTree;
    private WebView view;
    private String html;  // one way display: html -> webview

    public CardInfoTextView(CardItemsTree cardInfoTree) {
        view = new WebView();
        getChildren().add(view);

        this.cardInfoTree = cardInfoTree;
        // setEditable(false);

        // subscribe
        this.cardInfoTree.getSelectionModel().selectedItemProperty().addListener(new javafx.beans.value.ChangeListener<TreeItem>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem> observable, TreeItem oldValue, TreeItem newValue) {
                if (newValue == null) {
                    // clear output?
                    return;
                }

                // a lot of bs code...
                var nodeValue = ((TreeItem)newValue).getValue();
                if (nodeValue instanceof CardItemGeneralInformationModel) {
                    processValue((CardItemGeneralInformationModel)nodeValue);
                } else if (nodeValue instanceof CardItemEmvSFIModel) {
                    processValue((CardItemEmvSFIModel)nodeValue);
                } else if (nodeValue instanceof CardItemSFIModel) {
                    processValue((CardItemSFIModel)nodeValue);
                } else if (nodeValue instanceof CardItemAdfFCIModel) {
                    processValue((CardItemAdfFCIModel)nodeValue);
                } else if (nodeValue instanceof CardItemFCIModel) {
                    processValue((CardItemFCIModel)nodeValue);
                } else if (nodeValue instanceof CardItemPGPModel) {
                    processValue((CardItemPGPModel)nodeValue);
                } else if (nodeValue instanceof CardItemPSE1FCIModel) {
                    processValue((CardItemPSE1FCIModel)nodeValue);
                }
                // System.out.println(nodeValue.getClass().getName());
                // processValue(nodeValue);
            }
        });
    }

    private void processValue(CardItemGeneralInformationModel value) {
        processValue((CardItemRootModel)value);

        var sb = new StringBuilder(getText());
        sb.append(String.format("<p>ATR: %s</p>", Util.hexify(value.getAtr())));
        var atr = new ATR(value.getAtr());
        sb.append("<pre>").append(atr.parseToText()).append("</pre>");
        sb.append("\n");
        setText(sb.toString());
    }

    private void processValue(CardItemSFIModel value) {
        processValue((CardItemRootModel)value);
        var records = value.getRecords();

        var sb = new StringBuilder(getText());
        sb.append("Raw EF data\n\n");
        sb.append(String.format("  Total records: %d\n", records.size()));

        var iter = records.listIterator();
        while (iter.hasNext()) {
            sb.append(String.format("    Record %d: %s\n", iter.nextIndex(), 
                Util.hexify(iter.next())));
        }
        setText(sb.toString());
    }

    private void processValue(CardItemEmvSFIModel value) {
        processValue((CardItemRootModel)value);
        var sb = new StringBuilder(getText());
        var readObjects = new ArrayList<BerTlv>(10);

        for (var record : value.getRecords()) {
            try {
                var recordTlv = BerTlv.parseBytes(record);
                if (!recordTlv.tagEquals("70")) {
                    continue;
                }
                for (BerTlv p : recordTlv.getParts()) {
                    readObjects.add(p);
                }
            } catch (BerTlv.ParsingException e) {
                Util.errorLog("processValue(CardItemEmvSFIModel): failed to parse");
            } catch (BerTlv.ConstraintException e) {
                Util.errorLog("processValue(CardItemEmvSFIModel): failed to parse (constraint)");
            }
        }

        var mappedValues = Util.mapEmvDataObjects(readObjects);

        for (var b : readObjects) {
            String tagString = Util.hexify(b.getTag());
            sb.append(String.format("    %s\n", mappedValues.get(tagString)));
        }
        setText(sb.toString());
    }

    private void processValue(CardItemFCIModel value) {
        processValue((CardItemRootModel)value);

        var sb = new StringBuilder(getText());
        var fciData = value.getFciData();

        try {
            var root = BerTlv.parseBytes(fciData);
            sb.append("<p>").append("Decoded BER-TLV data:\n").append("</p>");
            sb.append("<pre>");
            sb.append(root.toString());
            sb.append("</pre>");
        // } catch (BerTlv.ConstraintException e) {
        //     sb.append(String.format("Failed to parse FCI data: %s\n", e));
        } catch (BerTlv.ParsingException e) {
            // sb.append(String.format("Failed to parse FCI data: %s\n", e));
            sb.append("<p>").append("Failed to decode data as FCI object.").append("</p>");
            sb.append("<p>").append("Raw data:").append("</p>");

            sb.append("<pre>").append(Util.hexify(fciData, 16)).append("</pre>");
        }
        setText(sb.toString());
    }

    private void processValue(CardItemAdfFCIModel value) {
        if (value.type == ApplicationInfoModel.TYPE.YK) {
            processValue((CardItemRootModel)value);
            // special processing of Yubikey
            // see https://developers.yubico.com/OATH/YKOATH_Protocol.html
            var sb = new StringBuilder(getText());
            var fciData = value.getFciData();
            sb.append("<p>").append("Yubikey application").append("</p>");
            sb.append("<p>").append("Challenge data:").append("</p>");
            sb.append("<pre>");
            if (fciData[0] == 0x79) {
                try {
                    for (SimpleTlv part: SimpleTlv.parseBytes(fciData)) {
                        switch (part.getTag()) {
                        case 0x79:
                            sb.append(String.format("  Version: %s\n", Util.hexify(part.getValue())));
                            break;
                        case 0x71:
                            sb.append(String.format("  Name: %s\n", Util.hexify(part.getValue())));
                            break;
                        case 0x74:
                            sb.append(String.format("  Challenge: %s\n", Util.hexify(part.getValue())));
                            break;
                        case 0x7B:
                            sb.append(String.format("  Algorithm: %s\n", Util.hexify(part.getValue())));
                            break;
                        default:
                            sb.append(String.format("  Unknown block (0x%02X): %s\n", 
                                part.getTag(), Util.hexify(part.getValue())));
                        }
                    }
                } catch (SimpleTlv.ParsingException e) {
                    sb.append("SIMPLE-TLV parse failed.\n");
                    sb.append(String.format("Raw data: %s\n", Util.hexify(fciData)));
                }
            } else {
                sb.append("Failed to parse YKOATH SELECT instruction result.\n");
                sb.append(String.format("Raw data: %s\n", Util.hexify(fciData)));
            }
            sb.append("</pre>");
            setText(sb.toString());
        } else {
            processValue((CardItemFCIModel)value);

            var sb = new StringBuilder(getText());
            sb.append(String.format("Application name: %s%n", value.name));
            setText(sb.toString());
        }
    }

    private void processValue(CardItemPGPModel value) {
        processValue((CardItemRootModel)value);

        var sb = new StringBuilder(getText());
        sb.append("<p>").append("OpenPGP data objects:").append("</p>");
        var iter = value.getDataObjects().entrySet().iterator();

        sb.append("<pre>");
        while (iter.hasNext()) {
            Map.Entry pair = (Map.Entry)iter.next();

            switch ((String)pair.getKey()) {
            case "4F":
                sb.append(String.format("Full AID (4F): %s\n", Util.hexify((byte[])pair.getValue())));
                break;
            case "5F 52":
                sb.append(String.format("Historical bytes (5F 52): %s\n", Util.hexify((byte[])pair.getValue())));
                break;
            case "C4":
                sb.append(String.format("PW status bytes (C4): %s\n", Util.hexify((byte[])pair.getValue())));
                break;
            case "6E":
                sb.append(String.format("Application Related Data (6E):\n %s\n", Util.hexify((byte[])pair.getValue(), 16)));
                break;
            case "7F 74":
                sb.append(String.format("General feature management (7F 74): %s\n", Util.hexify((byte[])pair.getValue())));
                break;
            case "5E":
                sb.append(String.format("Login data (5E): %s\n", Util.hexify((byte[])pair.getValue())));
                break;
            case "65":
                sb.append(String.format("Cardholder Related Data (65): %s\n", Util.hexify((byte[])pair.getValue())));
                break;
            case "5F 50":
                sb.append(String.format("URL (5F 50): %s\n", Util.hexify((byte[])pair.getValue())));
                break;
            case "7A":
                sb.append(String.format("Security support template (7A): %s\n", Util.hexify((byte[])pair.getValue())));
                break;
            }
        }
        sb.append("</pre>");
        setText(sb.toString());
    }

    private void processValue(CardItemPSE1FCIModel value) {
        processValue((CardItemFCIModel)value);
    }

    private void processValue(CardItemRootModel value) {
        var sb = new StringBuilder();
        sb.append("<h3>").append(value.getTitle()).append("</h3>");
        // text += value.getTitle() + "\n\n";
        setText(sb.toString());
    }

    private String getText() {
        return html;
    }

    private void setText(String text) {
        html = text;
        view.getEngine().loadContent(html);
    }
}