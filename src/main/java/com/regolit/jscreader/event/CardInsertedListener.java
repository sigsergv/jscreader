/* INSERT LICENSE HERE */

package com.regolit.jscreader.event;

import java.util.EventListener;

public interface CardInsertedListener extends EventListener {
    public void cardInserted(ChangeEvent e);
}