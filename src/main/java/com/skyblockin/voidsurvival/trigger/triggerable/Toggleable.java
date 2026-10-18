package com.skyblockin.voidsurvival.trigger.triggerable;

public interface Toggleable {

    void toggle();

    void setState(boolean toggled);

    boolean isToggled();

}
