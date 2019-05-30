/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemGPFCIModel extends CardItemFCIModel {
    private final byte[] aid;
    private final String name;

    public CardItemGPFCIModel(String title, byte[] fciData, byte[] aid, String name) {
        super(title, fciData);
        this.aid = aid;
        this.name = name;
    }

    public byte[] getAid() {
        return aid;
    }

    public String getName() {
        return name;
    }
}