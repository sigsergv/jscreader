/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;

import java.util.List;

/**
 * Basic class that represents EF with SFI
 */
public class CardItemSFIModel extends CardItemRootModel {
    public List<byte[]> records;

    public CardItemSFIModel(String title, List<byte[]> records) {
        super(title);
        this.records = records;
    }
}