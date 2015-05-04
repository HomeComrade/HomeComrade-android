package com.homecomrade;

import com.homecomrade.R;

public class ModelServerTypes {
    static public String TOTEM_SERVER = "totem";
    static public String OMXPLAYER_SERVER = "omxplayer";

    public ModelServerTypes() {

    }

    public boolean isValidServerType(String serverType) {
        if (serverType.equals(TOTEM_SERVER)) {
            return true;
        }
        else if (serverType.equals(OMXPLAYER_SERVER)) {
            return true;
        }

        return false;
    }

    public int getMenuForRemote(String serverType) {
        if (serverType.equals(TOTEM_SERVER)) {
            return R.menu.remote_totem;
        }
        else if (serverType.equals(OMXPLAYER_SERVER)) {
            return R.menu.remote_omxplayer;
        }

        return R.menu.remote_totem;
    }
}