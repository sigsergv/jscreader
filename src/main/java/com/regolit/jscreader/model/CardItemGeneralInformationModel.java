/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemGeneralInformationModel extends CardItemRootModel {
    private final byte[] atr;

    public CardItemGeneralInformationModel(String title, byte[] atr) {
        super(title);
        this.atr = atr;
    }

    public byte[] getAtr() {
        return atr;
    }
}