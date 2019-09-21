package com.github.yqy7.jhotkey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.Carbon;
import com.sun.jna.platform.mac.Carbon.EventHandlerProcPtr;
import com.sun.jna.platform.mac.Carbon.EventHotKeyID;
import com.sun.jna.platform.mac.Carbon.EventTypeSpec;
import com.sun.jna.ptr.PointerByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.yqy7.jhotkey.HotKeyVK.*;

class HotKeyRegisterMac extends HotKeyRegister {
    private static final Logger logger = LoggerFactory.getLogger(HotKeyRegisterMac.class);

    private AtomicInteger hotKeyIdGen = new AtomicInteger(1);
    private Map<Integer, MacHotKey> hotKeyMap = new HashMap<>();

    private static final int kEventClassKeyboard = OS_TYPE("keyb");
    private static final int kEventHotKeyPressed = 5;
    private static final int typeEventHotKeyID = OS_TYPE("hkid");
    private static final int kEventParamDirectObject = OS_TYPE("----");

    // 保存事件处理器的引用
    private PointerByReference eventHandlerOutRef = new PointerByReference();

    @Override
    protected void init() {
        // 安装HotKey事件处理器

        EventTypeSpec[] eventTypes = (EventTypeSpec[])(new EventTypeSpec().toArray(1));
        eventTypes[0].eventClass = kEventClassKeyboard;
        eventTypes[0].eventKind = kEventHotKeyPressed;

        int status = Carbon.INSTANCE.InstallEventHandler(Carbon.INSTANCE.GetEventDispatcherTarget(),
            new Eventhandler(), 1, eventTypes, null, eventHandlerOutRef);

        if (status != 0) {
            logger.error("Install hot key event handler error: " + status);
        }
    }

    class Eventhandler implements EventHandlerProcPtr {

        @Override
        public int callback(Pointer inHandlerCallRef, Pointer inEvent, Pointer inUserData) {
            // 获取事件参数
            EventHotKeyID eventHotKeyID = new EventHotKeyID();
            int status = Carbon.INSTANCE.GetEventParameter(inEvent, kEventParamDirectObject, typeEventHotKeyID,
                null, eventHotKeyID.size(), null, eventHotKeyID);

            System.out.println(eventHotKeyID.id);

            if (status != 0) {
                logger.error("Get event parameter error: " + status);
            } else {
                int id = eventHotKeyID.id;
                fireEvent(hotKeyMap.get(id));
            }
            return 0;
        }
    }

    // 注册HotKey
    private void registerHotKey(MacHotKey hotKey) {
        int id = hotKeyIdGen.getAndIncrement();
        EventHotKeyID.ByValue hotKeyReference = new EventHotKeyID.ByValue();

        hotKeyReference.id = id;
        hotKeyReference.signature = OS_TYPE("hk" + String.format("%02d", id));

        PointerByReference hotKeyOutRef = new PointerByReference();

        int status = Carbon.INSTANCE.RegisterEventHotKey(
            KeyMapMac.converKeyCode(hotKey.getKeyCode()), KeyMapMac.convertModifiers(hotKey.getModifiers()),
            hotKeyReference, Carbon.INSTANCE.GetEventDispatcherTarget(), 0, hotKeyOutRef);

        System.out.println("register status: " + status);

        if (status != 0) {
            logger.error("Register hot key error: " + status);
            return;
        }

        hotKey.setOutRef(hotKeyOutRef);
        hotKeyMap.put(id, hotKey);
        System.out.println(hotKeyMap);
    }

    private static int OS_TYPE(String osType) {
        byte[] bytes = osType.getBytes();
        return (bytes[0] << 24) + (bytes[1] << 16) + (bytes[2] << 8) + bytes[3];
    }

    @Override
    protected synchronized void register(HotKey hotKey) {
        registerHotKey(new MacHotKey(hotKey));
    }

    @Override
    public synchronized void removeAllHotKey() {
        for (MacHotKey hotKey : hotKeyMap.values()) {
            int status = Carbon.INSTANCE.UnregisterEventHotKey(hotKey.getOutRef().getValue());
            if (status != 0) {
                logger.warn("Unregister HotKey error: " + status);
            }
        }

        hotKeyMap.clear();
    }

    @Override
    public synchronized void stop() {
        removeAllHotKey();
        if (eventHandlerOutRef.getValue() != null) {
            Carbon.INSTANCE.RemoveEventHandler(eventHandlerOutRef.getValue());
        }
        super.stop();
    }

}

class MacHotKey extends HotKey {
    // 删除HotKey需要用到注册时返回的outRef
    private PointerByReference outRef;

    public MacHotKey(HotKey hotKey) {
        setKeyCode(hotKey.getKeyCode());
        setModifiers(hotKey.getModifiers());
        setListener(hotKey.getListener());
    }

    public PointerByReference getOutRef() {
        return outRef;
    }

    public void setOutRef(PointerByReference outRef) {
        this.outRef = outRef;
    }
}

class KeyMapMac {
    private static Map<Integer, Integer> keyCodeMap = new HashMap<Integer, Integer>() {{
        // 字母
        put(VK_A, 0);
        put(VK_B, 11);
        put(VK_C, 8);
        put(VK_D, 2);
        put(VK_E, 14);
        put(VK_F, 3);
        put(VK_G, 5);
        put(VK_H, 4);
        put(VK_I, 34);
        put(VK_J, 38);
        put(VK_K, 40);
        put(VK_L, 37);
        put(VK_M, 46);
        put(VK_N, 45);
        put(VK_O, 31);
        put(VK_P, 35);
        put(VK_Q, 12);
        put(VK_R, 15);
        put(VK_S, 1);
        put(VK_T, 17);
        put(VK_U, 32);
        put(VK_V, 9);
        put(VK_W, 13);
        put(VK_X, 7);
        put(VK_Y, 16);
        put(VK_Z, 6);

        // 数字
        put(VK_1, 18);
        put(VK_2, 19);
        put(VK_3, 20);
        put(VK_4, 21);
        put(VK_5, 23);
        put(VK_6, 22);
        put(VK_7, 26);
        put(VK_8, 28);
        put(VK_9, 25);
        put(VK_0, 29);

        // F1 ~ F12
        put(VK_F1, 122);
        put(VK_F2, 120);
        put(VK_F3, 99);
        put(VK_F4, 118);
        put(VK_F5, 96);
        put(VK_F6, 97);
        put(VK_F7, 98);
        put(VK_F8, 100);
        put(VK_F9, 101);
        put(VK_F10, 109);
        put(VK_F11, 103);
        put(VK_F12, 111);
    }};

    static int convertModifiers(int javaModifiers) {
        int modifiers = 0;
        if ((javaModifiers & HotKeyModifier.META_MASK) != 0) {
            modifiers |= Carbon.cmdKey;
        }

        if ((javaModifiers & HotKeyModifier.CTRL_MASK) != 0) {
            modifiers |= Carbon.controlKey;
        }

        if ((javaModifiers & HotKeyModifier.ALT_MASK) != 0) {
            modifiers |= Carbon.optionKey;
        }

        if ((javaModifiers & HotKeyModifier.SHIFT_MASK) != 0) {
            modifiers |= Carbon.shiftKey;
        }

        return modifiers;
    }

    static int converKeyCode(int javaKeyCode) {
        return keyCodeMap.get(javaKeyCode);
    }
}