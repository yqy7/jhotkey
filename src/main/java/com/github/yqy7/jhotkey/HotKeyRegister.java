package com.github.yqy7.jhotkey;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.jna.Platform;

public abstract class HotKeyRegister {
    // 单例，类初始化时就创建好实现类的实例并初始化，避免初始化实例的时候的同步问题 <br/>
    // 实现类确保包内可见，不要被外部类访问到
    private static HotKeyRegister INSTANCE;

    static {
        System.setProperty("jna.nosys", "true");

        if (Platform.isWindows()) {
            INSTANCE = new HotKeyRegisterWin();
        }

        if (Platform.isMac()) {
            INSTANCE = new HotKeyRegisterMac();
        }

        if (Platform.isLinux()) {
            INSTANCE = new HotKeyRegisterLinux();
        }

        INSTANCE.init();
    }

    public static HotKeyRegister getInstance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }

        throw new UnsupportedOperationException("This platform is not supported!");
    }

    private ExecutorService eventExecutor = Executors.newSingleThreadExecutor();

    protected void fireEvent(HotKey hotKey) {
        eventExecutor.execute(() -> {
            hotKey.getListener().onAction(hotKey);
        });
    }

    protected abstract void init();

    protected abstract void register(HotKey hotKey);

    /**
     * hotkeyString的格式： meta ctrl shift alt key <br/> meta即 windows 的 win 键或者 mac 的 command 键 <br/> key为 0-9 或者 a-z 或者
     * F1-F12 <br/> 例1： meta a <br/> 例2： meta ctrl 9 <br/> 例3： meta ctrl alt 3 <br/> 例4： meta ctrl shift alt F1 <br/>
     *
     * @param hotkeyString
     * @param hotKeyListener
     */
    public void register(String hotkeyString, HotKeyListener hotKeyListener) {
        HotKey hotKey = HotKey.parseHotKey(hotkeyString);
        hotKey.setListener(hotKeyListener);
        register(hotKey);
    }

    /**
     * 清除所有 HotKey
     */
    public abstract void removeAllHotKey();

    /**
     * 清除所有 HotKey 并停止后台线程
     */
    public void stop() {
        eventExecutor.shutdown();
    }
}
