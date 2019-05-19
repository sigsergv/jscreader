/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.event.ChangeEvent;
import com.regolit.jscreader.event.CardInsertedListener;
import com.regolit.jscreader.event.CardRemovedListener;

import javafx.scene.control.TreeView;

class CardItemsTree extends TreeView
    implements CardInsertedListener, CardRemovedListener
{
    public CardItemsTree() {
        // subscribe to card inserted event
        var dm = DeviceManager.getInstance();
        dm.addListener(this);
    }

    public void cardInserted(ChangeEvent e) {
        System.err.println("Card inserted");
    }

    public void cardRemoved(ChangeEvent e) {
        System.err.println("Card removed");
    }
}