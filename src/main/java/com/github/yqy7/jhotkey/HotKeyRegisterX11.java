package com.github.yqy7.jhotkey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.platform.unix.X11.Window;
import com.sun.jna.platform.unix.X11.XEvent;
import com.sun.jna.platform.unix.X11.XKeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.yqy7.jhotkey.HotKeyVK.VK_0;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_1;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_2;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_3;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_4;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_5;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_6;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_7;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_8;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_9;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_A;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_B;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_C;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_D;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_E;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F1;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F10;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F11;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F12;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F2;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F3;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F4;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F5;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F6;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F7;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F8;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_F9;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_G;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_H;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_I;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_J;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_K;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_L;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_M;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_N;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_O;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_P;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_Q;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_R;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_S;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_T;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_U;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_V;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_W;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_X;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_Y;
import static com.github.yqy7.jhotkey.HotKeyVK.VK_Z;
import static com.sun.jna.platform.unix.X11.ControlMask;
import static com.sun.jna.platform.unix.X11.Mod1Mask;
import static com.sun.jna.platform.unix.X11.Mod4Mask;
import static com.sun.jna.platform.unix.X11.ShiftMask;

class HotKeyRegisterX11 extends HotKeyRegister {
    private static final Logger logger = LoggerFactory.getLogger(HotKeyRegisterX11.class);
    private static final X11 x11 = X11.INSTANCE;

    private volatile boolean stopped = false;
    private List<HotKey> hotKeyList = new ArrayList<>();
    private Display display;
    private Window window;
    private Thread thread;
    private CountDownLatch prepareLatch = new CountDownLatch(1);

    @Override
    protected void init() {
        thread = new Thread(() -> {
            // 初始化
            display = x11.XOpenDisplay(null);
            x11.XSetErrorHandler((display, errorEvent) -> {
                byte[] buf = new byte[1024];
                int len = 0;
                while (buf[len] != 0) {
                    len++;
                }
                x11.XGetErrorText(display, errorEvent.error_code, buf, buf.length);
                logger.error("ErrorHandler: " + new String(buf, 0, len));
                return 0;
            });

            window = x11.XDefaultRootWindow(display);

            prepareLatch.countDown();

            // 监听事件
            XEvent event = new XEvent();
            while (!stopped) {
                while(x11.XPending(display) > 0) {
                    x11.XNextEvent(display, event);
                    if (event.type != X11.KeyPress)
                        continue;

                    XKeyEvent xkey = (XKeyEvent) event.readField("xkey");
                    int state = xkey.state & (ShiftMask | ControlMask | Mod1Mask | Mod4Mask);

                    for (HotKey hotKey : hotKeyList) {
                        byte keyCode = KeyMapX11.convertKeyCode(hotKey.getKeyCode(), display);
                        int modifiers = KeyMapX11.convertModifiers(hotKey.getModifiers());
                        if (keyCode == (byte) xkey.keycode && modifiers == state) {
                            fireEvent(hotKey);
                        }
                    }
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(300);
                } catch (InterruptedException e) {
                }
            }

            x11.XCloseDisplay(display);
            logger.info("JHotKey Thread exit.");
        }, "JHotKey Thread");

        thread.start();
    }

    @Override
    protected synchronized void register(HotKey hotKey) {
        try {
            prepareLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte keyCode = KeyMapX11.convertKeyCode(hotKey.getKeyCode(), display);
        int modifiers = KeyMapX11.convertModifiers(hotKey.getModifiers());

        x11.XGrabKey(display, keyCode, modifiers, window, 1, x11.GrabModeAsync, x11.GrabModeAsync);
        hotKeyList.add(hotKey);
    }

    @Override
    public synchronized void removeAllHotKey() {
        for (HotKey hotKey : hotKeyList) {
            byte keyCode = KeyMapX11.convertKeyCode(hotKey.getKeyCode(), display);
            int modifiers = KeyMapX11.convertModifiers(hotKey.getModifiers());
            x11.XUngrabKey(display, keyCode, modifiers, window);
        }

        hotKeyList.clear();
    }

    @Override
    public synchronized void stop() {
        stopped = true;
        super.stop();
    }

}

class KeyMapX11 {
    private static X11 x11 = X11.INSTANCE;

    private static Map<Integer, String> keyCodeMap = new HashMap<Integer, String>() {{
        // 字母
        put(VK_A, "A");
        put(VK_B, "B");
        put(VK_C, "C");
        put(VK_D, "D");
        put(VK_E, "E");
        put(VK_F, "F");
        put(VK_G, "G");
        put(VK_H, "H");
        put(VK_I, "I");
        put(VK_J, "J");
        put(VK_K, "K");
        put(VK_L, "L");
        put(VK_M, "M");
        put(VK_N, "N");
        put(VK_O, "O");
        put(VK_P, "P");
        put(VK_Q, "Q");
        put(VK_R, "R");
        put(VK_S, "S");
        put(VK_T, "T");
        put(VK_U, "U");
        put(VK_V, "V");
        put(VK_W, "W");
        put(VK_X, "X");
        put(VK_Y, "Y");
        put(VK_Z, "Z");

        // 数字
        put(VK_1, "1");
        put(VK_2, "2");
        put(VK_3, "3");
        put(VK_4, "4");
        put(VK_5, "5");
        put(VK_6, "6");
        put(VK_7, "7");
        put(VK_8, "8");
        put(VK_9, "9");
        put(VK_0, "0");

        // F1 ~ F12
        put(VK_F1, "F1");
        put(VK_F2, "F2");
        put(VK_F3, "F3");
        put(VK_F4, "F4");
        put(VK_F5, "F5");
        put(VK_F6, "F6");
        put(VK_F7, "F7");
        put(VK_F8, "F8");
        put(VK_F9, "F9");
        put(VK_F10, "F10");
        put(VK_F11, "F11");
        put(VK_F12, "F12");
    }};

    static int convertModifiers(int javaModifiers) {
        int modifiers = 0;
        if ((javaModifiers & HotKeyModifier.META_MASK) != 0) {
            modifiers |= Mod4Mask;
        }

        if ((javaModifiers & HotKeyModifier.CTRL_MASK) != 0) {
            modifiers |= ControlMask;
        }

        if ((javaModifiers & HotKeyModifier.ALT_MASK) != 0) {
            modifiers |= Mod1Mask;
        }

        if ((javaModifiers & HotKeyModifier.SHIFT_MASK) != 0) {
            modifiers |= ShiftMask;
        }

        return modifiers;
    }

    static byte convertKeyCode(int javaKeyCode, Display display) {
        X11.KeySym keySym = x11.XStringToKeysym(keyCodeMap.get(javaKeyCode));
        byte keycode = x11.XKeysymToKeycode(display, keySym);
        return keycode;
    }
}


