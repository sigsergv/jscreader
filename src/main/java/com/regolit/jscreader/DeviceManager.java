/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.event.ChangeListener;
import com.regolit.jscreader.event.CardInsertedListener;
import com.regolit.jscreader.event.CardRemovedListener;
import com.regolit.jscreader.event.ChangeEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import javax.smartcardio.TerminalFactory;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardException;
import java.lang.Thread;
import java.util.EventListener;
import javafx.application.Platform;


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
    private List<EventListener> listeners = new ArrayList<EventListener>();
    private String selectedTerminalName = null;
    private Boolean selectedTerminalCardInserted = null;
    private CardMonitoringThread monitoringThread;

    private class CardMonitoringThread extends Thread {
        CardTerminal terminal;
        public CardMonitoringThread(CardTerminal terminal) {
            this.terminal = terminal;
        }
    }

    private DeviceManager() {
        terminalNames = new ArrayList<>(5);
        timer = new Timer(true);
        locked = false;
        terminalFactory = TerminalFactory.getDefault();  // questionable, because lock factory at start
        monitoringThread = null;

        // execute task every second
        var me = this;
        timer.schedule(new TimerTask() {
            public void run() {
                // execute in GUI thread
                Platform.runLater(me::reloadTerminalDevices);
            }
        }, 1000, 1000);

        // // start card monitoring thread
        // var monitoringThread = new Thread() {
        //     public void run() {
        //         System.err.println("Card monitoring thread started");
        //     }
        // };
        // monitoringThread.start();
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
        if (terminalNames.indexOf(name) == -1) {
            return;
        }
        selectedTerminalName = name;
        fireChangedEvent();
    }

    public CardTerminal getTerminal(String name) {
        var terminals = terminalFactory.terminals();
        return terminals.getTerminal(name);
    }

    public synchronized void addListener(EventListener l) {
        listeners.add(l);
    }

    /**
     * Refresh terminals list.
     * Update card status of selected terminal (inserted or not).
     */
    private void reloadTerminalDevices() {
        if (locked) {
            return;
        }
        locked = true;
        try {
            var terminals = terminalFactory.terminals().list();
            var newTerminalNames = new ArrayList<String>(terminals.size());
            var terminalsMap = new java.util.HashMap<String, CardTerminal>(terminals.size());

            for (CardTerminal t : terminals) {
                var n = t.getName();
                newTerminalNames.add(n);
                terminalsMap.put(n, t);
            }

            if (!newTerminalNames.equals(terminalNames)) {
                // boolean restartThreadFlag = false;
                terminalNames = newTerminalNames;
                if (terminalNames.indexOf(selectedTerminalName) == -1) {
                    selectedTerminalName = null;
                }
                if (selectedTerminalName == null && terminalNames.size() > 0) {
                    selectedTerminalName = terminalNames.get(0);
                }

                fireChangedEvent();
            }
            if (selectedTerminalName == null) {
                selectedTerminalCardInserted = null;
            } else {
                var t = terminalsMap.get(selectedTerminalName);
                if (t != null) {
                    if (selectedTerminalCardInserted == null) {
                        // i.e. there is no information about status
                        selectedTerminalCardInserted = t.isCardPresent();
                        if (selectedTerminalCardInserted) {
                            fireCardInsertedEvent();
                        }
                    } else {
                        // i.e. previous card state is known
                        var p = t.isCardPresent();
                        if (selectedTerminalCardInserted != p) {
                            if (p) {
                                fireCardInsertedEvent();
                            } else {
                                fireCardRemovedEvent();
                            }
                            selectedTerminalCardInserted = p;
                        }
                    }
                }
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

    private synchronized void fireCardInsertedEvent() {
        var event = new ChangeEvent(this);

        for (Object l : listeners) {
            if (l instanceof CardInsertedListener) {
                ((CardInsertedListener)l).cardInserted(event);
            }
        }
    }
    
    private synchronized void fireCardRemovedEvent() {
        var event = new ChangeEvent(this);

        for (Object l : listeners) {
            if (l instanceof CardRemovedListener) {
                ((CardRemovedListener)l).cardRemoved(event);
            }
        }
    }
}