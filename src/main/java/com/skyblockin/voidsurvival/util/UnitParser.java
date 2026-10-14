package com.skyblockin.voidsurvival.util;

import com.skyblockin.voidsurvival.config.Pair;

import java.util.*;

public final class UnitParser {

    private final ArrayList<Pair<String, Long>> units = new ArrayList<>();
    private final String baseUnit;
    private String previousUnit;

    public UnitParser(String baseUnit) {
        this.baseUnit = baseUnit;
        this.previousUnit = baseUnit;
        this.units.add(new Pair<>(baseUnit, 1L));
    }

    /**
     *
     * @param newUnit The name of the new unit
     * @param value the value in the provided units
     * @param unit the unit which value is provided in
     * @return this parser
     */
    public UnitParser ofUnit(String newUnit, long value, String unit) {
        return addUnit(newUnit, getUnitValue(unit) * value);
    }

    /**
     *
     * @param unit the name of the new unit
     * @param value the value in the previously added unit. If no unit was added yet, this method will use the base unit.
     * @return this parser
     */
    public UnitParser ofPrevious(String unit, long value) {
        return addUnit(unit, getUnitValue(this.previousUnit) * value);
    }

    public UnitParser addUnit(String unit, long baseValue) {
        if (baseValue <= 0) {
            throw new IllegalArgumentException("Unit value must be a positive non-zero integer");
        }
        if (unit.equals(baseUnit)) {
            throw new IllegalArgumentException("Modifying the base unit is not allowed!");
        }

        previousUnit = unit;
        units.add(new Pair<>(unit, baseValue));

        // Ensure the underlying array remains in ascending order
        if (units.size() > 1 && units.get(units.size() - 2).right() < units.getLast().right()) {
            units.sort((a, b) -> Long.compare(a.right(), b.right()) * -1);
        }

        return this;
    }

    public long getUnitValue(String unit) {

        for (Pair<String, Long> pair : this.units) {
            if (pair.left().equals(unit)) {
                return pair.right();
            }
        }

        throw new IllegalArgumentException("Attempted to get value for undefined unit '" + unit + "'");
    }

    public String getBaseUnit() {
        return this.baseUnit;
    }

    public long parse(String s) {

        if (!Character.isDigit(s.charAt(0))) {
            throw new IllegalArgumentException("Unit string must start with a digit");
        }

        long total = 0;
        int pos = 0;
        int end = 0;

        while (pos < s.length()) {

            // It should start with a digit, so reading until the unit should happen first
            while (end < s.length() && Character.isDigit(s.charAt(end))) {
                end++;
            }

            long value = Long.parseLong(s, pos, end, 10);

            // Might have reached the end here
            if (end == s.length()) {
                total += value;
                break;
            }

            // There may be spaces between the value and the unit, so let's handle that
            while (end < s.length() && Character.isSpaceChar(s.charAt(end))) {
                end++;
            }
            pos = end;

            // Skip until next digit
            while (end < s.length() && Character.isLetter(s.charAt(end))) {
                end++;
            }

            String unit = s.substring(pos, end);

            long unitValue = getUnitValue(unit);

            if (!unit.equals(baseUnit)) {
                total += value * unitValue;
            } else {
                total += value;
            }

            while (end < s.length() && Character.isSpaceChar(s.charAt(end))) {
                end++;
            }
            pos = end;
        }

        return total;
    }

    public double parseAsDouble(String s, String unit) {

        long result = parse(s);

        return (double) result / (double) getUnitValue(unit);
    }

    public long parse(String s, String unit) {

        long result = parse(s);

        return result / getUnitValue(unit);
    }

    public String toUnitString(long value) {

        if (value == 0) {
            return "0" + this.baseUnit;
        }

        // Just handle negative values as positive values and prepend a minus sign if it was negative
        boolean wasNegative = value < 0;
        if (wasNegative) {
            value = Math.abs(value);
        }

        StringBuilder sb = new StringBuilder();

        for (Pair<String, Long> unit : this.units) {

            long units = value / unit.right();

            if (units != 0) {
                value -= units * unit.right();
                sb.append(units).append(unit.left()).append(' ');
            }

        }

        sb.deleteCharAt(sb.length() - 1);

        if (wasNegative) {
            return "-" + sb;
        }

        return sb.toString();
    }
}
