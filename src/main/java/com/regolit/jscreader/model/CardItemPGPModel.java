/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;

import java.util.Map;

public class CardItemPGPModel extends CardItemRootModel {
    public Map<String, byte[]> dataObjects;

    public CardItemPGPModel(String title, Map<String, byte[]> dataObjects) {
        super(title);
        this.dataObjects = dataObjects;
    }
}