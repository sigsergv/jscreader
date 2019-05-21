/* INSERT LICENSE HERE */

package com.regolit.jscreader.model;

import com.regolit.jscreader.util.Util;

public class ApplicationInfoModel {
    public enum TYPE {
        EMV,
        UNKNOWN
    };

    public final byte[] aid;
    public final TYPE type;
    public final String name;

    public ApplicationInfoModel(String aid, String type, String name) {
        this.aid = Util.toByteArray(aid);
        this.name = name;
        if (type == "emv") {
            this.type = TYPE.EMV;
        } else {
            this.type = TYPE.UNKNOWN;
        }
    }
}