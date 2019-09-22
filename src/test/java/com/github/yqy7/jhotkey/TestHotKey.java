package com.github.yqy7.jhotkey;

import java.awt.*;

import javax.swing.*;

public class TestHotKey {
    public static void main(String[] args) throws InterruptedException {
        // -Djava.awt.headless=true设置了也会运行失败
        //Toolkit
        //KeyStroke.getKeyStroke("control J"); // 需要加载awt库，否则在mac下不工作

        //Thread thread = new Thread(() -> {
        //    //System.loadLibrary("awt"); // 本质上是需要加载awt库
        //    KeyStroke.getKeyStroke("control J");
        //    System.out.println("加载awt库");
        //});
        //thread.start();
        //thread.join();


        HotKeyRegister instance = HotKeyRegister.getInstance();
        instance.register("ctrl J", hotKey -> {
            System.out.println("press hotkey: " + hotKey);
        });

        Thread.sleep(10000);

        instance.removeAllHotKey();
        System.out.println("清除所有.....hotkey。。。");

        Thread.sleep(10000);
        instance.stop();

        Thread.sleep(10000);
        System.out.println("close...");

    }
}
