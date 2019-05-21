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
    private boolean selectedTerminalCardInserted = false;
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
                    // restartThreadFlag = true;
                }
                if (selectedTerminalName == null && terminalNames.size() > 0) {
                    selectedTerminalName = terminalNames.get(0);
                    // restartThreadFlag = true;
                }

                if (selectedTerminalName == null) {
                    selectedTerminalCardInserted = false;
                } else {
                    var t = terminalsMap.get(selectedTerminalName);
                    if (t != null) {
                        var p = t.isCardPresent();
                        if (selectedTerminalCardInserted != p) {
                            if (p) {
                                fireCardInsertedEvent();
                            } else {
                                fireCardRemovedEvent();
                            }
                        }
                    }
                }
                // if (restartThreadFlag) {
                //     restartCardMonitoringThread();
                // }
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
    

    // these two methods below are really required, because current implementation of PCSCTerminals.java
    // uses single SCARDCONTEXT for all threads/terminals
    private boolean terminalWaitForCardPresent(CardTerminal terminal, long timeout)
        throws CardException
    {
        if (timeout == 0) {
            timeout = 3600000;  // one hour
        }
        long limitTime = System.currentTimeMillis() + timeout;

        while (true) {
            if (System.currentTimeMillis() > limitTime) {
                return false;
            }
            if (terminal.waitForCardPresent(1000) == false) {
                continue;
            } else {
                return true;
            }
        }
    }

    private boolean terminalWaitForCardAbsent(CardTerminal terminal, long timeout)
        throws CardException
    {
        if (timeout == 0) {
            timeout = 3600000;  // one hour
        }
        long limitTime = System.currentTimeMillis() + timeout;

        while (true) {
            if (System.currentTimeMillis() > limitTime) {
                return false;
            }
            if (terminal.waitForCardAbsent(1000) == false) {
                continue;
            } else {
                return true;
            }
        }
    }

    // private void restartCardMonitoringThread() {
    //     System.err.println("restartCardMonitoringThread() call");
    //     // stop existing thread
    //     if (monitoringThread != null) {
    //         var t = monitoringThread;
    //         monitoringThread = null;
    //         t.interrupt();
    //     }
    //     var name = getSelectedTerminalName();
    //     if (name == null) {
    //         return;
    //     } 
    //     var terminal = getTerminal(name);
    //     if (terminal == null) {
    //         return;
    //     }
    //     var thread = new CardMonitoringThread(terminal) {
    //         public void run() {
    //             try {
    //                 for (int i=0; i<10000; i++) {
    //                     terminalWaitForCardPresent(this.terminal, 0);
    //                     fireCardInsertedEvent();
    //                     terminalWaitForCardAbsent(this.terminal, 0);
    //                     fireCardRemovedEvent();
    //                 }
    //             } catch (CardException e) {
    //                 System.err.printf("Failed to monitor card on terminal: `%s`%n", this.terminal.getName());
    //             }
    //         }
    //     };
    //     monitoringThread = thread;
    //     monitoringThread.start();
    // }
}