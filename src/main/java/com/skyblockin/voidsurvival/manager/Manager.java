package com.skyblockin.voidsurvival.manager;

import com.skyblockin.voidsurvival.VoidSurvival;

import java.io.IOException;

public abstract class Manager {

    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void onEnable(VoidSurvival plugin) {
    }

    public void onDisable(VoidSurvival plugin) {
    }

    public void reload(VoidSurvival plugin) throws IOException {}

}
