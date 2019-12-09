package cn.nlifew.clipmgr.util;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectUtils {
    private static final String TAG = "ReflectUtils";

    private ReflectUtils() {  }

    public static Method getDeclaredMethod(Class<?> cls, String name, Class<?>... params) {
        Method method;
        try {
            method = cls.getDeclaredMethod(name, params);
            method.setAccessible(true);
        } catch (NoSuchMethodException e) {
            method = null;
            Log.e(TAG, "getDeclaredMethod: " + name, e);
        }
        return method;
    }

    public static Field getDeclaredField(Class<?> cls, String name) {
        Field field;
        try {
            field = cls.getDeclaredField(name);
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            field = null;
            Log.e(TAG, "getDeclaredField: " + name, e);
        }
        return field;
    }
}
