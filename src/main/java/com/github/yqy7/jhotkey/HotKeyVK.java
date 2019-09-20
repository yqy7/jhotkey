package com.github.yqy7.jhotkey;

import java.lang.reflect.Field;

/**
 * 从 java.awt.event.KeyEvent中拷贝过来，只选择支持部分常用键
 */
public class HotKeyVK {

    // 字母
    public static final int VK_A = 0x41;
    public static final int VK_B = 0x42;
    public static final int VK_C = 0x43;
    public static final int VK_D = 0x44;
    public static final int VK_E = 0x45;
    public static final int VK_F = 0x46;
    public static final int VK_G = 0x47;
    public static final int VK_H = 0x48;
    public static final int VK_I = 0x49;
    public static final int VK_J = 0x4A;
    public static final int VK_K = 0x4B;
    public static final int VK_L = 0x4C;
    public static final int VK_M = 0x4D;
    public static final int VK_N = 0x4E;
    public static final int VK_O = 0x4F;
    public static final int VK_P = 0x50;
    public static final int VK_Q = 0x51;
    public static final int VK_R = 0x52;
    public static final int VK_S = 0x53;
    public static final int VK_T = 0x54;
    public static final int VK_U = 0x55;
    public static final int VK_V = 0x56;
    public static final int VK_W = 0x57;
    public static final int VK_X = 0x58;
    public static final int VK_Y = 0x59;
    public static final int VK_Z = 0x5A;

    // 数字
    public static final int VK_0 = 0x30;
    public static final int VK_1 = 0x31;
    public static final int VK_2 = 0x32;
    public static final int VK_3 = 0x33;
    public static final int VK_4 = 0x34;
    public static final int VK_5 = 0x35;
    public static final int VK_6 = 0x36;
    public static final int VK_7 = 0x37;
    public static final int VK_8 = 0x38;
    public static final int VK_9 = 0x39;

    // F1~F12
    public static final int VK_F1 = 0x70;
    public static final int VK_F2 = 0x71;
    public static final int VK_F3 = 0x72;
    public static final int VK_F4 = 0x73;
    public static final int VK_F5 = 0x74;
    public static final int VK_F6 = 0x75;
    public static final int VK_F7 = 0x76;
    public static final int VK_F8 = 0x77;
    public static final int VK_F9 = 0x78;
    public static final int VK_F10 = 0x79;
    public static final int VK_F11 = 0x7A;
    public static final int VK_F12 = 0x7B;

    // TODO 添加其他常用键
    // ` - = [ ] \ ; ' , . /
    // tab enter capsLock esc backspace

    // 这个方法不应该被调用很多次，会影响运行速度
    public static Integer getKeyCode(String s) {
        try {
            s = "VK_" + s.toUpperCase();
            Field field = HotKeyVK.class.getField(s);
            return field.getInt(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getKeyCodeText(int keyCode) {
        if (keyCode >= VK_A && keyCode <= VK_Z) {
            return (char)(keyCode - VK_A + 'A') + "";
        }

        if (keyCode >= VK_0 && keyCode <= VK_9) {
            return (char)(keyCode - VK_0 + '0') + "";
        }

        if (keyCode >= VK_F1 && keyCode <= VK_F12) {
            return "F" + (keyCode - VK_F1 + 1);
        }

        return null;
    }
}
