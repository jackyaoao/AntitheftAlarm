package com.antitheft.alarm.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.antitheft.alarm.AppContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

public class MyPrefs {

    private static MyPrefs instance = null;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private MyPrefs() {
        sp = AppContext.getContext().getSharedPreferences(Const.PREF_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static MyPrefs getInstance() {
        if (instance == null) {
            synchronized (MyPrefs.class) {
                if (instance == null) {
                    return new MyPrefs();
                }
            }
        }
        return instance;
    }

    public void put(String key, Object value) {
        if (sp != null) {
            if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            }
            if (value instanceof String) {
                editor.putString(key, (String) value);
            }
            if (value instanceof Integer) {
                editor.putInt(key, ((Integer) value).intValue());
            }
            if (value instanceof Float) {
                editor.putFloat(key, ((Float) value).floatValue());
            }
            if (value instanceof Long) {
                editor.putLong(key, ((Long) value).longValue());
            }
            if (value instanceof Set<?>) {
                editor.putStringSet(key, (Set<String>) value);
            }
            editor.commit();
        }
    }

    public void serial(String key, Serializable serial) {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(key));
            out.writeObject(serial);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Serializable deserial(String key) {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(key));
            return (Serializable) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean getBoolean(String key) {
        return sp.getBoolean(key, false);
    }

    public int getInt(String key) {
        return sp.getInt(key, -1);
    }

    public long getLong(String key) {
        return sp.getLong(key, 0);
    }

    public String getString(String key) {
        return sp.getString(key, null);
    }

    public Float getFloat(String key) {
        return sp.getFloat(key, 0);
    }

    public Set<String> getStringSet(String key) {
        return sp.getStringSet(key, null);
    }

    public Serializable getSerial(String key) {
        return deserial(key);
    }
}
