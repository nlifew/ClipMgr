package cn.nlifew.clipmgr.core;


import android.content.ClipData;
import android.content.IClipboard;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import de.robv.android.xposed.XposedBridge;

final class IClipboardImpl implements InvocationHandler {
    private static final String TAG = "IClipboardImpl";

    IClipboardImpl(IClipboard impl) {
        mImpl = impl;
    }

    private final IClipboard mImpl;
    private final XSetPrimaryClip mX = new XSetPrimaryClip();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String name = method.getName();
        XposedBridge.log(TAG + ": invoke: " + name + " " + Arrays.toString(args));

        if (Objects.equals(name, "setPrimaryClip")) {
            mX.setPrimaryClip(mImpl, method, args);
            return null;
        }

        // 不要忘了调用原函数
        return method.invoke(mImpl, args);
    }

    IClipboard getClipboard() {
        return mImpl;
    }
}
