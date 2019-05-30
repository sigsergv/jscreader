/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;

import com.regolit.jscreader.util.Util;

public class ApplicationInfoModel {
    public enum TYPE {
        EMV,
        YK,
        GP, // GlobalPlatform
        UNKNOWN
    };

    private final byte[] aid;
    private final TYPE type;
    private final String name;
    private final boolean enabled;

    public ApplicationInfoModel(String aid, String type, String name, boolean enabled) {
        this.aid = Util.toByteArray(aid);
        this.enabled = enabled;
        this.name = name;
        if (type.equals("emv")) {
            this.type = TYPE.EMV;
        } else if (type.equals("GP")) {
            this.type = TYPE.GP;
        } else if (type.equals("yk")) {
            this.type = TYPE.YK;
        } else {
            this.type = TYPE.UNKNOWN;
        }
    }

    public byte[] getAid() {
        return aid;
    }

    public TYPE getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean getEnabled() {
        // return false;
        return enabled;
    }
}