/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;


public class CardItemRootModel {
    public String title;
    public CardItemRootModel(String title) {
        this.title = title;
    }
    @Override public String toString() {
        return title;
    }
}