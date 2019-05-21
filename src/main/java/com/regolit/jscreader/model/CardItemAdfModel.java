/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemAdfModel extends CardItemRootModel {
    public byte[] aid;
    public String name;
    public ApplicationInfoModel.TYPE type;

    public CardItemAdfModel(String title, byte[] aid, ApplicationInfoModel.TYPE type, String name) {
        super(title);
        
        this.aid = aid;
        this.name = name;
        this.type = type;
    }
}