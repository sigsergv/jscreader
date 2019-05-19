/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.event.ChangeListener;
import com.regolit.jscreader.event.ChangeEvent;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

/**
 * Implements PC/SC device selection.
 *
 * It doesn't store selected item, DeviceManager does this instead.
 */
class DeviceSelector extends MenuButton implements ChangeListener {
    private String value;
    private EventHandler<ActionEvent> menuEvent;

    public DeviceSelector() {
        var dm = DeviceManager.getInstance();
        dm.addListener(this);
        menuEvent = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                var dm = DeviceManager.getInstance();
                var source = (MenuItem)e.getSource();
                dm.setSelectedTerminalName(source.getId());
            }
        };
    }

    public void stateChanged(ChangeEvent e) {
        // fetch list of terminals and reload internal popup with them
        // also select first one
        var dm = DeviceManager.getInstance();
        var terminalNames = dm.getTerminalNames();

        var items = getItems();
        var currentValue = dm.getSelectedTerminalName();

        // to avoid "java.lang.IllegalStateException: Not on FX application thread"
        Platform.runLater(() -> {
            items.clear();
            var cm = getContextMenu();
            if (cm != null) {
                cm.hide();
            }
            for (var name : terminalNames) {
                var item = new MenuItem(String.format("Terminal: %s", name));
                item.setOnAction(menuEvent);
                item.setId(name);
                items.add(item);
            }
            if (currentValue == null) {
                    setText("");
            } else {
                setText(currentValue);
            }
        });
    }
}