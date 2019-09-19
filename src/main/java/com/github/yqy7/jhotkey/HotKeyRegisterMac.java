package com.github.yqy7.jhotkey;

class HotKeyRegisterMac extends HotKeyRegister {

    @Override
    protected void init() {
        throw new UnsupportedOperationException("This platform is not supported!");
    }

    @Override
    protected synchronized void register(HotKey hotKey) {

    }

    @Override
    public synchronized void removeAllHotKey() {

    }

    @Override
    public void stop() {
        super.stop();
    }

}
