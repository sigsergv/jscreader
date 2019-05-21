/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemAdfFCIModel extends CardItemFCIModel {
    public byte[] aid;
    public String name;
    public ApplicationInfoModel.TYPE type;

    public CardItemAdfFCIModel(String title, byte[] fciData, byte[] aid, ApplicationInfoModel.TYPE type, String name) {
        super(title, fciData);
        
        this.aid = aid;
        this.name = name;
        this.type = type;
    }
}