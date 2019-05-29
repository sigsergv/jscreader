/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;

import java.util.Map;

public class CardItemPGPModel extends CardItemRootModel {
    private final Map<String, byte[]> dataObjects;

    public CardItemPGPModel(String title, Map<String, byte[]> dataObjects) {
        super(title);
        this.dataObjects = dataObjects;
    }

    public Map<String, byte[]> getDataObjects() {
        return dataObjects;
    }
}