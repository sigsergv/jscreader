/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemEmvFCIModel extends CardItemFCIModel {
    private final byte[] aipData;
    private final byte[] aflData;

    public CardItemEmvFCIModel(String title, byte[] fciData, byte[] aipData, byte[] aflData) {
        super(title, fciData);
        this.aipData = aipData;
        this.aflData = aflData;
    }

    public byte[] getAipData() {
        return aipData;
    }

    public byte[] getAflData() {
        return aflData;
    }
}