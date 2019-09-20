package com.github.yqy7.jhotkey;

public class HotKeyModifier {
    public static final int CTRL_MASK = 1 << 1;
    public static final int META_MASK = 1 << 2;
    public static final int ALT_MASK = 1 << 3;
    public static final int SHIFT_MASK = 1 << 0;

    public static Integer getModifier(String s) {
        switch (s.toUpperCase()) {
            case "CTRL":
                return CTRL_MASK;
            case "META":
                return META_MASK;
            case "ALT":
                return ALT_MASK;
            case "SHIFT":
                return SHIFT_MASK;
            default:
                return null;
        }
    }

    public static String getModifiersText(int modifiers) {
        StringBuilder stringBuilder = new StringBuilder();

        if ((modifiers & META_MASK) != 0) {
            stringBuilder.append(" META");
        }

        if ((modifiers & CTRL_MASK) != 0) {
            stringBuilder.append(" CTRL");
        }

        if ((modifiers & ALT_MASK) != 0) {
            stringBuilder.append(" ALT");
        }

        if ((modifiers & SHIFT_MASK) != 0) {
            stringBuilder.append(" SHIFT");
        }

        if (stringBuilder.length() == 0) {
            return null;
        } else {
            return stringBuilder.substring(1);
        }
    }
}
