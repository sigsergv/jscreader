/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemRootModel {
    private final String title;
    public CardItemRootModel(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override public String toString() {
        return title;
    }
}