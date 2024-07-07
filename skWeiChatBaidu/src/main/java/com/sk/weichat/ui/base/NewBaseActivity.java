package com.sk.weichat.ui.base;

/**
 * @Author Roken
 * @Time 2022/9/3
 * @Describe 描述
 */

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.sk.weichat.AppConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.User;
import com.sk.weichat.util.LocaleHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class NewBaseActivity extends ActionBackActivity implements CoreStatusListener {
    public CoreManager coreManager;
    private List<CoreStatusListener> coreStatusListeners;
    private boolean loginRequired = true;
    private boolean configRequired = true;

    protected void noLoginRequired() {
        Log.d(TAG, "noLoginRequired() called");
        loginRequired = false;
    }

    protected void loginRequired() {
        Log.d(TAG, "loginRequired() called");
        loginRequired = true;
    }

    protected void noConfigRequired() {
        Log.d(TAG, "noConfigRequired() called");
        configRequired = false;
    }

    // 注册CoreManager初始化状态的监听，比如fragment可以调用，
    public void addCoreStatusListener(CoreStatusListener listener) {
        coreStatusListeners.add(listener);
    }

    public User getUser() {
        return coreManager.getSelf();
    }

    public String getToken() {
        return coreManager.getSelfStatus().accessToken;
    }

    public AppConfig getAppConfig() {
        return coreManager.getConfig();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCore();
    }

    protected void initCore() {
        Log.d(TAG, "initCore() called");
        if (coreManager == null) {
            coreManager = new CoreManager(this, this);
        }
        if (coreStatusListeners == null) {
            coreStatusListeners = new ArrayList<>();
        }
        //coreManager.init(loginRequired, configRequired);
    }

    @Override
    public void onCoreReady() {
        Log.d(TAG, "onCoreReady() called");
        for (CoreStatusListener listener : coreStatusListeners) {
            listener.onCoreReady();
        }
    }

    @Override
    protected void onDestroy() {
        coreManager.destroy();
        super.onDestroy();
    }
}