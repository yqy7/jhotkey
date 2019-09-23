package com.github.yqy7.jhotkey;

public class TestHotKey {
    public static void main(String[] args) throws InterruptedException {

        HotKeyRegister instance = HotKeyRegister.getInstance();
        instance.register("ctrl J", hotKey -> {
            System.out.println("press hotkey: " + hotKey);
            System.out.println(Thread.currentThread());
        });

        instance.register("ctrl F4", hotKey -> {
            System.out.println("press hotkey: " + hotKey);
            System.out.println(Thread.currentThread());
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
