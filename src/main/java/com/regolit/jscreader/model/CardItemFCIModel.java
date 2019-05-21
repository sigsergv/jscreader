/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemFCIModel extends CardItemRootModel {
    public byte[] fciData;

    public CardItemFCIModel(String title, byte[] fciData) {
        super(title);
        this.fciData = fciData;
    }
}