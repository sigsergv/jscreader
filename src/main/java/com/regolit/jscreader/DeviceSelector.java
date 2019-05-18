/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.event.ChangeListener;
import com.regolit.jscreader.event.ChangeEvent;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.application.Platform;

/**
 * Implements PC/SC device selection.
 */
class DeviceSelector extends MenuButton implements ChangeListener {
    private String value;

    public DeviceSelector() {
        var dm = DeviceManager.getInstance();
        value = null;  // means no terminals selected
        dm.addChangeListener(this);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void stateChanged(ChangeEvent e) {
        // fetch list of terminals and reload internal popup with them
        // also select first one
        var dm = DeviceManager.getInstance();
        var terminalNames = dm.getTerminalNames();

        var items = getItems();
        var currentValue = getValue();

        // to avoid "java.lang.IllegalStateException: Not on FX application thread"
        Platform.runLater(() -> {
            items.clear();
            var cm = getContextMenu();
            if (cm != null) {
                cm.hide();
            }
            for (var name : terminalNames) {
                items.add(new MenuItem(name));
            }
            if (currentValue==null || terminalNames.indexOf(currentValue)==-1) {
                if (terminalNames.size() == 0) {
                    // clear
                    value = null;
                    setText("");
                } else {
                    var first = terminalNames.get(0);
                    setText(first);
                    value = first;
                }
            }
        });
    }
}