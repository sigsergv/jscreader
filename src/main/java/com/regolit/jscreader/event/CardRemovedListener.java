/* INSERT LICENSE HERE */

package com.regolit.jscreader.event;

import java.util.EventListener;

public interface CardRemovedListener extends EventListener {
    public void cardRemoved(ChangeEvent e);
}