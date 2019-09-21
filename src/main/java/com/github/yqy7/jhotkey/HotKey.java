package com.github.yqy7.jhotkey;

import java.util.StringTokenizer;

public class HotKey {
    private int modifiers = 0;
    private int keyCode;
    private HotKeyListener listener;

    protected HotKey() { }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public HotKeyListener getListener() {
        return listener;
    }

    public void setListener(HotKeyListener listener) {
        this.listener = listener;
    }

    /**
     * s的格式： meta ctrl shift alt key <br/>
     * meta即 windows 的 win 键或者 mac 的 command 键 <br/>
     * key为 0-9 或者 a-z 或者 F1-f12 <br/>
     * 例1： meta a <br/>
     * 例2： meta ctrl 9 <br/>
     * 例3： meta ctrl alt 3 <br/>
     * 例4： meta ctrl shift alt F1 <br/>
     *
     * @param s
     * @return
     */
    public static HotKey parseHotKey(String s) {
        StringTokenizer stringTokenizer = new StringTokenizer(s, " ");
        if (stringTokenizer.countTokens() > 5) {
            throw new RuntimeException("Hotkey Parse Error: " + s);
        }

        String token;
        HotKey hotKey = new HotKey();
        while (stringTokenizer.hasMoreTokens()) {
            token = stringTokenizer.nextToken();

            if (HotKeyModifier.getModifier(token) != null) {
                hotKey.modifiers |= HotKeyModifier.getModifier(token);
            }

            if (HotKeyVK.getKeyCode(token) != null) {
                hotKey.keyCode = HotKeyVK.getKeyCode(token);
            }
        }

        return hotKey;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        String modifiersText = HotKeyModifier.getModifiersText(modifiers);
        if (modifiersText != null) {
            stringBuilder.append(" ");
            stringBuilder.append(modifiersText);
        }

        String keyCodeText = HotKeyVK.getKeyCodeText(keyCode);
        if (keyCodeText != null) {
            stringBuilder.append(" ");
            stringBuilder.append(keyCodeText);
        }

        if (stringBuilder.length() == 0) {
            return "null(modifiers) null(keyCode)";
        } else {
            return stringBuilder.substring(1);
        }
    }
}
