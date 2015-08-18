package com.zhh;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by Administrator on 2014/8/14.
 */
public class MySharedPreferences {
    static MySharedPreferences instance;

    public static MySharedPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new MySharedPreferences(context);
        }
        return instance;
    }

    private MySharedPreferences(Context context) {
        s = context.getSharedPreferences("AppleRaider", Context.MODE_PRIVATE);
    }

    SharedPreferences s;

    public Object getAttribute(String key) {
        return s.getAll().get(key);
    }

    private void setString(String key, String value) {
        s.edit().putString(key, value).commit();
    }

    private void setInt(String key, int value) {
        s.edit().putInt(key, value).commit();
    }

    private void setFloat(String key, float value) {
        s.edit().putFloat(key, value).commit();
    }

    private void setBoolean(String key, boolean value) {
        s.edit().putBoolean(key, value).commit();
    }

    private void setLong(String key, long value) {
        s.edit().putLong(key, value).commit();
    }
    public void clear() {
        s.edit().clear().commit();
    }

    public void setAttribute(String key, Object value) {
        if (value instanceof String) {
            setString(key, (String) value);
        } else if (value instanceof Integer) {
            setInt(key, (Integer) value);
        } else if (value instanceof Float) {
            setFloat(key, (Float) value);
        } else if (value instanceof Boolean) {
            setBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            setLong(key, (Long) value);
        }
    }

}
