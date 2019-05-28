/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;

import java.util.List;

/**
 * Basic class that represents EF with SFI
 */
public class CardItemEmvSFIModel extends CardItemSFIModel {
    public CardItemEmvSFIModel(String title, List<byte[]> records) {
        super(title, records);
    }
}