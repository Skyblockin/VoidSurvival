package com.skyblockin.voidsurvival.util;

import java.util.concurrent.TimeUnit;

public class CustomTimeUnit {

    public static final CustomTimeUnit MILLISECONDS = new CustomTimeUnit(TimeUnit.MILLISECONDS);
    public static final CustomTimeUnit TICKS = new CustomTimeUnit(TimeUnit.MILLISECONDS);
    public static final CustomTimeUnit SECONDS = new CustomTimeUnit(TimeUnit.SECONDS);
    public static final CustomTimeUnit MINUTES = new CustomTimeUnit(TimeUnit.MINUTES);
    public static final CustomTimeUnit HOURS = new CustomTimeUnit(TimeUnit.HOURS);
    public static final CustomTimeUnit DAYS = new CustomTimeUnit(TimeUnit.DAYS);

    private final TimeUnit baseUnit;

    public CustomTimeUnit(TimeUnit baseUnit) {
        this.baseUnit = baseUnit;
    }

    public long toMillis(long value) {
        return convert(value, TimeUnit.MILLISECONDS);
    }

    public long convert(long value, TimeUnit to) {

        if (this == TICKS) {
            return to.convert(value * 50, baseUnit);
        }

        return to.convert(value, baseUnit);
    }

    public long convert(long value, CustomTimeUnit to) {

        if (this == to) {
            return value;
        }

        if (to == TICKS) {
            return baseUnit.toMillis(value) / 50;
        }

        return convert(value, to.baseUnit);
    }

}
