package com.sk.weichat.sp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * SharedPreferences 存取的封装
 *
 * @author roamer
 */
public class CommonSp {
    private SharedPreferences mSharePre;

    public CommonSp(Context context, String spName) {
        mSharePre = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    protected SharedPreferences getSharePreference() {
        return mSharePre;
    }

    /* get and put String value */
    public void setValue(String key, String value) {
        if (UserSp.KEY_ACCESS_TOKEN.equals(key)) {
            // 这里 apply属于延迟方法，后续如果有紧接着的请求，可能由于未同步到存储文件，导致使用旧数据，这里使用commit(),立即写入文件
            mSharePre.edit().putString(key, value).commit();
        } else {
            mSharePre.edit().putString(key, value).apply();
        }
    }

    public String getValue(String key, String defValue) {
        return mSharePre.getString(key, defValue);
    }

    /* get and put boolean value */
    public void setValue(String key, boolean value) {
        mSharePre.edit().putBoolean(key, value).apply();
    }

    public boolean getValue(String key, boolean defValue) {
        return mSharePre.getBoolean(key, defValue);
    }

    /* get and put int value */
    public void setValue(String key, int value) {
        mSharePre.edit().putInt(key, value).apply();
    }

    public int getValue(String key, int defValue) {
        return mSharePre.getInt(key, defValue);
    }

    /* get and put int value */
    public void setValue(String key, float value) {
        mSharePre.edit().putFloat(key, value).apply();
    }

    public float getValue(String key, float defValue) {
        return mSharePre.getFloat(key, defValue);
    }

    /* get and put long value */
    public void setValue(String key, long value) {
        mSharePre.edit().putLong(key, value).apply();
    }

    public long getValue(String key, long defValue) {
        return mSharePre.getLong(key, defValue);
    }

    /* get and put string set value */
    public void setValue(String key, Set<String> value) {
        mSharePre.edit().putStringSet(key, value).apply();
    }

    public Set<String> getValue(String key, Set<String> defValue) {
        return mSharePre.getStringSet(key, defValue);
    }

    /* 添加和移除sp改变的监听 */
    public void registerSpChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mSharePre.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterSpChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mSharePre.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
