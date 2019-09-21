package com.github.yqy7.jhotkey;

import java.awt.*;

import javax.swing.*;

public class TestHotKey {
    public static void main(String[] args) throws InterruptedException {
        KeyStroke.getKeyStroke("control J"); // 需要触发打开awt，否则在mac下不工作

        HotKeyRegister instance = HotKeyRegister.getInstance();
        instance.register("ctrl J", hotKey -> {
            System.out.println("press hotkey: " + hotKey);
        });

        Thread.sleep(10000);

        //instance.removeAllHotKey();
        System.out.println("清除所有.....hotkey。。。");

        Thread.sleep(10000);
        //instance.stop();

        Thread.sleep(10000);
        System.out.println("close...");

    }
}
