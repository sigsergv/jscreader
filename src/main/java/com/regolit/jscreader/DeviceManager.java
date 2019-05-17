/* INSERT LICENSE HERE */

package com.regolit.jscreader;

/**
 * Maintains PC/SC terminals list: detect changes etc.
 *
 * Simple singleton.
 */
class DeviceManager {
    private static DeviceManager instance = null;

    public static DeviceManager getInstance() {
        if (instance == null) {
            instance = new DeviceManager();
        }
        return instance;
    }
}