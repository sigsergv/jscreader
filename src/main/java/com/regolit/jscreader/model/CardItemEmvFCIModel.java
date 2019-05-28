/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemEmvFCIModel extends CardItemFCIModel {
    public byte[] aid;
    public String name;
    public ApplicationInfoModel.TYPE type;
    private byte[] aipData;
    private byte[] aflData;

    public CardItemEmvFCIModel(String title, byte[] fciData, byte[] aipData, byte[] aflData) {
        super(title, fciData);
        this.aipData = aipData;
        this.aflData = aflData;
    }
}