package com.github.yqy7.jhotkey;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.MSG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HotKeyRegisterWin extends HotKeyRegister {
    private static Logger logger = LoggerFactory.getLogger(HotKeyRegisterWin.class);

    private static int WM_USER_REGISTER_HOTKEY = 1;
    private static int WM_USER_CLEAR_HOTKEY = 2;

    private Map<Integer, HotKey> hotKeyMap = new HashMap<>();
    private Queue<HotKey> registerQueue = new LinkedList<>();
    private AtomicInteger hotKeyIdGen = new AtomicInteger(0);
    private volatile int nativeThreadId = -1;
    private CountDownLatch prepareLatch = new CountDownLatch(1);

    @Override
    protected void init() {

        Thread thread = new Thread(() -> {
            MSG msg = new MSG();

            User32.INSTANCE.PeekMessage(msg, null, 0, 0, 1); // 触发一下，使线程变成GUI线程
            nativeThreadId = Kernel32.INSTANCE.GetCurrentThreadId();

            prepareLatch.countDown(); // 准备完成，可以注册了

            // 消息循环
            while (User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Windows Message: \n" + msg.toString());
                }

                // 处理hotkey消息
                if (msg.message == WinUser.WM_HOTKEY) {
                    int id = msg.wParam.intValue();
                    HotKey hotKey = hotKeyMap.get(id);
                    if (hotKey == null) { continue; }

                    fireEvent(hotKey);
                }

                if (msg.message == WinUser.WM_USER && msg.wParam.intValue() == WM_USER_REGISTER_HOTKEY) {
                    while (!registerQueue.isEmpty()) {
                        registerHotKey(registerQueue.poll());
                    }
                }

                if (msg.message == WinUser.WM_USER && msg.wParam.intValue() == WM_USER_CLEAR_HOTKEY) {
                    clearHotKeys();
                }

                if (msg.message == WinUser.WM_CLOSE) {
                    clearHotKeys();
                    break;
                }
            }
        }, "JHotKey Thread");
        thread.setDaemon(true);
        thread.start();
    }

    // 清除所有注册的 hotkey
    private void clearHotKeys() {
        for (Integer id : hotKeyMap.keySet()) {
            User32.INSTANCE.UnregisterHotKey(null, id);
        }
        hotKeyMap.clear();
    }

    // 注册hotkey
    private void registerHotKey(HotKey hotKey) {
        int id = hotKeyIdGen.incrementAndGet();
        if (User32.INSTANCE.RegisterHotKey(null, id,
            KeyMapWin.convertModifiers(hotKey.getModifiers()),
            KeyMapWin.converKeyCode(hotKey.getKeyCode()))) {
            hotKeyMap.put(id, hotKey);
            logger.info("Registering HotKey: " + hotKey);
        } else {
            logger.warn("Unable to register HotKey: " + hotKey);
        }
    }

    @Override
    protected synchronized void register(HotKey hotKey) {
        registerQueue.offer(hotKey);
        try {
            prepareLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        User32.INSTANCE.PostThreadMessage(nativeThreadId, WinUser.WM_USER, new WPARAM(WM_USER_REGISTER_HOTKEY),
            new LPARAM());
    }

    @Override
    public synchronized void removeAllHotKey() {
        try {
            prepareLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        User32.INSTANCE.PostThreadMessage(nativeThreadId, WinUser.WM_USER, new WPARAM(WM_USER_CLEAR_HOTKEY),
            new LPARAM());
    }

    @Override
    public void stop() {
        try {
            prepareLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        User32.INSTANCE.PostThreadMessage(nativeThreadId, WinUser.WM_CLOSE, new WPARAM(), new LPARAM());
        super.stop();
    }

}

class KeyMapWin {
    static int convertModifiers(int javaModifiers) {
        int winModifiers = 0;
        if ((javaModifiers & HotKeyModifier.META_MASK) != 0) {
            winModifiers |= WinUser.MOD_WIN;
        }

        if ((javaModifiers & HotKeyModifier.CTRL_MASK) != 0) {
            winModifiers |= WinUser.MOD_CONTROL;
        }

        if ((javaModifiers & HotKeyModifier.ALT_MASK) != 0) {
            winModifiers |= WinUser.MOD_ALT;
        }

        if ((javaModifiers & HotKeyModifier.SHIFT_MASK) != 0) {
            winModifiers |= WinUser.MOD_SHIFT;
        }

        String os = System.getProperty("os.version", "");
        if (os.compareTo("6.1") >= 0) {
            winModifiers |= WinUser.MOD_NOREPEAT;
        }

        return winModifiers;
    }

    // 目前支持的键还不需要转换
    static int converKeyCode(int javaKeyCode) {
        return javaKeyCode;
    }
}
