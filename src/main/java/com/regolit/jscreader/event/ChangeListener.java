/* INSERT LICENSE HERE */

package com.regolit.jscreader.event;

import java.util.EventListener;

public interface ChangeListener extends EventListener {
    public void stateChanged(ChangeEvent e);
}