package com.github.yqy7.jhotkey;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.MSG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HotKeyRegisterWin extends HotKeyRegister {
    private static Logger logger = LoggerFactory.getLogger(HotKeyRegisterWin.class);

    private Map<Integer, HotKey> hotKeyMap;
    private Queue<HotKey> registerQueue;
    private AtomicInteger hotkeyIdGen;
    private volatile boolean stoped;
    private boolean clearAllHotKey;

    @Override
    protected void init() {
        hotKeyMap = new HashMap<>();
        registerQueue = new LinkedList<>();
        hotkeyIdGen = new AtomicInteger(0);
        stoped = false;
        clearAllHotKey = false;

        Thread thread = new Thread(() -> {
            MSG msg = new MSG();
            while (!stoped) {
                // 检查消息
                while (User32.INSTANCE.PeekMessage(msg, null, 0, 0, 1)) {
                    // 处理hotkey消息
                    if (msg.message != WinUser.WM_HOTKEY) { continue; }

                    int id = msg.wParam.intValue();
                    HotKey hotKey = hotKeyMap.get(id);
                    if (hotKey == null) { continue; }

                    fireEvent(hotKey);
                }

                synchronized (HotKeyRegisterWin.this) {
                    while (!registerQueue.isEmpty()) {
                        // 注册hotkey
                        registerHotKey(registerQueue.poll());
                    }

                    if (clearAllHotKey) {
                        for (Integer id : hotKeyMap.keySet()) {
                            User32.INSTANCE.UnregisterHotKey(null, id);
                        }
                        hotKeyMap.clear();
                        clearAllHotKey = false;
                    }
                }

                Thread.yield();
            }

            // 线程停止前先把 HotKey 删除
            synchronized (HotKeyRegisterWin.this) {
                if (clearAllHotKey) {
                    for (Integer id : hotKeyMap.keySet()) {
                        User32.INSTANCE.UnregisterHotKey(null, id);
                    }
                    hotKeyMap.clear();
                }
            }

        }, "JHotKey Thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void registerHotKey(HotKey hotKey) {
        int id = hotkeyIdGen.incrementAndGet();
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
    }

    @Override
    public synchronized void removeAllHotKey() {
        clearAllHotKey = true;
    }

    @Override
    public void stop() {
        removeAllHotKey();
        stoped = true;
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
