package com.github.yqy7.jhotkey;

/**
 * 所有注册的 Listener 将会运行在同一个单独的线程中 <br/> 最好使用 swing 的 SwingUtilities.invokeLater 或者 javafx 的 Platform.runLater 等来运行业务逻辑 <br/>
 */
public interface HotKeyListener {
    void onAction(HotKey hotKey);
}
