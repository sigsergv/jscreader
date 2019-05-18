/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.event.ChangeListener;
import com.regolit.jscreader.event.ChangeEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.smartcardio.TerminalFactory;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardException;
import java.beans.EventHandler;


/**
 * Maintains PC/SC terminals list: detect changes etc. Also stores currently selected terminal.
 *
 * Simple singleton.
 */
final class DeviceManager {
    private static DeviceManager instance = null;

    private List<String> terminalNames;
    private Timer timer;
    private boolean locked;
    private TerminalFactory terminalFactory;
    private List listeners = new ArrayList();
    private String selectedTerminalName = null;


    private DeviceManager() {
        terminalNames = new ArrayList<>(5);
        timer = new Timer(true);
        locked = false;
        terminalFactory = TerminalFactory.getDefault();  // questionable, because lock factory at start

        // execute task every second
        timer.schedule(new TimerTask() {
            public void run() {
                reloadTerminalDevices();
            }
        }, 1000, 1000);
    }

    public static DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }

    public List<String> getTerminalNames() {
        return terminalNames;
    }

    public String getSelectedTerminalName() {
        return selectedTerminalName;
    }

    public void setSelectedTerminalName(String name) {
        if (name == selectedTerminalName) {
            return;
        }
        // check that terminal exists
        if (getTerminal(name) == null) {
            return;
        }
        selectedTerminalName = name;
        fireChangedEvent();
    }

    public CardTerminal getTerminal(String name) {
        var terminals = terminalFactory.terminals();
        return terminals.getTerminal(name);
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    private void reloadTerminalDevices() {
        if (locked) {
            return;
        }
        locked = true;
        try {
            var terminals = terminalFactory.terminals().list();
            var newTerminalNames = new ArrayList<String>(terminals.size());
            for (CardTerminal c : terminals) {
                newTerminalNames.add(c.getName());
            }
            if (!newTerminalNames.equals(terminalNames)) {
                terminalNames = newTerminalNames;
                if (terminalNames.indexOf(selectedTerminalName) == -1) {
                    selectedTerminalName = null;
                }
                if (selectedTerminalName == null && terminalNames.size() > 0) {
                    selectedTerminalName = terminalNames.get(0);
                }
                fireChangedEvent();
            }
        } catch (CardException e) {
            System.err.printf("reloadTerminalDevices failed: %s%n", e);
        }
        locked = false;
    }

    private synchronized void fireChangedEvent() {
        var event = new ChangeEvent(this);

        for (Object l : listeners) {
            if (l instanceof ChangeListener) {
                ((ChangeListener)l).stateChanged(event);
            }
        }
    }
}