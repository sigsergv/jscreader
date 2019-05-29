/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemFCIModel extends CardItemRootModel {
    private final byte[] fciData;

    public CardItemFCIModel(String title, byte[] fciData) {
        super(title);
        this.fciData = fciData;
    }

    public byte[] getFciData() {
        return fciData;
    }
}