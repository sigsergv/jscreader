/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;

import com.regolit.jscreader.util.Util;

public class ApplicationInfoModel {
    public enum TYPE {
        EMV,
        YK,
        PGP,
        UNKNOWN
    };

    public final byte[] aid;
    public final TYPE type;
    public final String name;

    public ApplicationInfoModel(String aid, String type, String name) {
        this.aid = Util.toByteArray(aid);
        this.name = name;
        if (type.equals("emv")) {
            this.type = TYPE.EMV;
        } else if (type.equals("yk")) {
            this.type = TYPE.YK;
        } else {
            this.type = TYPE.UNKNOWN;
        }
    }
}