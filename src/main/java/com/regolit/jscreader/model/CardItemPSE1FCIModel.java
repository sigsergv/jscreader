/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemPSE1FCIModel extends CardItemFCIModel {
    public byte[] aid;
    public String name;
    public ApplicationInfoModel.TYPE type;

    public CardItemPSE1FCIModel(String title, byte[] fciData) {
        super(title, fciData);
    }
}