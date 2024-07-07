package com.sk.weichat.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.sk.weichat.AppConfig;
import com.sk.weichat.AppConstant;
import com.sk.weichat.BuildConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.ConfigBean;
import com.sk.weichat.bean.event.MessageLogin;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.ui.account.SelectLoginModeActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.notification.NotificationProxyActivity;
import com.sk.weichat.ui.other.PrivacyAgreeActivity;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.VersionUtil;
import com.sk.weichat.view.PrivacyInfoDialog;
import com.sk.weichat.view.TipDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 启动页
 */
public class SplashActivity extends BaseActivity  {



    // 配置是否成功
    private boolean mConfigReady = false;

    public SplashActivity() {
        // 这个页面不需要已经获取config, 也不需要已经登录，
        noConfigRequired();
        noLoginRequired();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (NotificationProxyActivity.processIntent(intent)) {
            // 如果是通知点击进来的，带上参数转发给NotificationProxyActivity处理，
            intent.setClass(this, NotificationProxyActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        // 如果不是任务栈第一个页面，就直接结束，显示上一个页面，
        // 主要是部分设备上Jitsi_pre页面退后台再回来会打开这个启动页flg=0x10200000，此时应该结束启动页，回到Jitsi_pre,
        if (!isTaskRoot()) {
            finish();
            return;
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_splash);
        CoreManager.currLoginPipe = PreferenceUtils.getInt(this,Constants.APP_LOGIN_PIPE,0);
        // 初始化配置
        initConfig();

        EventBusHelper.register(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 请求权限过程中离开了回来就再请求吧，
        ready();
    }

    /**
     * 配置参数初始化
     */
    private void initConfig() {
        getConfig();
    }

    private void getConfig() {
        String mConfigApi = AppConfig.readConfigUrl(mContext);

        Map<String, String> params = new HashMap<>();
        Reporter.putUserData("configUrl", mConfigApi);
        long requestTime = System.currentTimeMillis();
        HttpUtils.get().url(mConfigApi)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<ConfigBean>(ConfigBean.class) {
                    @Override
                    public void onResponse(ObjectResult<ConfigBean> result) {
                        if (result != null) {
                            long responseTime = System.currentTimeMillis();
                            TimeUtils.responseTime(requestTime, result.getCurrentTime(), result.getCurrentTime(), responseTime);
                        }
                        ConfigBean configBean;
                        if (result == null || result.getData() == null || result.getResultCode() != Result.CODE_SUCCESS) {
                            Log.e("zq", "获取网络配置失败，使用已经保存了的配置");
                            if (BuildConfig.DEBUG) {
                                ToastUtil.showToast(SplashActivity.this, R.string.tip_get_config_failed);
                            }
                            // 获取网络配置失败，使用已经保存了的配置，
                            configBean = coreManager.readConfigBean();
                        } else {
                            Log.e("zq", "获取网络配置成功，使用服务端返回的配置并更新本地配置");
                            configBean = result.getData();
                            if (!TextUtils.isEmpty(configBean.getAddress())) {
                                PreferenceUtils.putString(SplashActivity.this, AppConstant.EXTRA_CLUSTER_AREA, configBean.getAddress());
                            }
                            coreManager.saveConfigBean(configBean);
                            MyApplication.IS_OPEN_CLUSTER = configBean.getIsOpenCluster() == 1 ? true : false;
                        }
                        setConfig(configBean);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e("zq", "获取网络配置失败，使用已经保存了的配置");
                        // ToastUtil.showToast(SplashActivity.this, R.string.tip_get_config_failed);
                        // 获取网络配置失败，使用已经保存了的配置，
                        ConfigBean configBean = coreManager.readConfigBean();
                        setConfig(configBean);
                    }
                });
    }

    private void setConfig(ConfigBean configBean) {
        if (configBean == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast(this, R.string.tip_get_config_failed);
            }

            // 如果没有保存配置，也就是第一次使用，就连不上服务器，使用默认配置
            configBean = CoreManager.getDefaultConfig(this);
            if (configBean == null) {
                // 不可到达，本地assets一定要提供默认config,
                DialogHelper.tip(this, getString(R.string.tip_get_config_failed));
                return;
            }
            coreManager.saveConfigBean(configBean);
        }

        // todo 定位权限放到应用内请求，class：LoginActivity、SelectAreaActivity、NearPersonActivity、MapPickerActivity
/*
        if (!coreManager.getConfig().disableLocationServer) {// 定位
            permissionsMap.put(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.permission_location);
            permissionsMap.put(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_location);
        }
*/
        // 配置完毕
        mConfigReady = true;
        MyApplication.IS_SUPPORT_SECURE_CHAT = configBean.getIsOpenSecureChat() == 1;
        // 如果没有androidDisable字段就不判断，
        // 当前版本没被禁用才继续打开，
        if (TextUtils.isEmpty(configBean.getAndroidDisable()) || !blockVersion(configBean.getAndroidDisable(), configBean.getAndroidAppUrl())) {
            // 进入主界面
            ready();
        }
    }

    /**
     * 如果当前版本被禁用，就自杀，
     *
     * @param disabledVersion 禁用该版本以下的版本，
     * @param appUrl          版本被禁用时打开的地址，
     * @return 返回是否被禁用，
     */
    private boolean blockVersion(String disabledVersion, String appUrl) {
        String currentVersion = BuildConfig.VERSION_NAME;
        if (VersionUtil.compare(currentVersion, disabledVersion) > 0) {
            // 当前版本大于被禁用版本，
            return false;
        } else {
            // 通知一下，
            TipDialog tipDialog = new TipDialog(this);
            tipDialog.setmConfirmOnClickListener(getString(R.string.tip_version_disabled), () -> {

            });
            tipDialog.setOnDismissListener(dialog -> {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(appUrl));
                    startActivity(i);
                } catch (Exception e) {
                    // 弹出浏览器失败的话无视，
                    // 比如没有浏览器的情况，
                    // 比如appUrl不合法的情况，
                }
                // 自杀，
                finish();
                MyApplication.getInstance().destory();
            });
            tipDialog.show();
            return true;
        }
    }

    PrivacyInfoDialog privacyInfoDialog;

    private void ready() {
        if (!mConfigReady) {// 配置失败
            return;
        }
        if(getSp(this).getBoolean("isReadPrivacy", false)){
            jump();
        }else{
            showPrivacyInfoDialog();
        }
    }

    private static SharedPreferences getSp(Context ctx) {
        return ctx.getSharedPreferences("privacy_info", Context.MODE_PRIVATE);
    }

    private void showPrivacyInfoDialog(){
        if(privacyInfoDialog == null){
//            HookMacAddressUtils.hookMacAddress(getApplicationContext());
//            HookMacAddressUtils.hookMacAddress(this);
            privacyInfoDialog = new PrivacyInfoDialog(this, new PrivacyInfoDialog.OnClickListener() {
                @Override
                public void onAgree() {

                    //if(isReadPrivacy1 && isReadPrivacy2){
                        getSp(SplashActivity.this).edit().putBoolean("isReadPrivacy", true).commit();
                        privacyInfoDialog.dismiss();
                        jump();
//                        HookMacAddressUtils.closeHookMacAddress();
                    /*}else{
                        ToastUtil.showToast(SplashActivity.this, "请仔细阅读《用户协议》和《隐私协议》");
                    }*/
                }

                @Override
                public void onCancel() {
                    privacyInfoDialog.dismiss();
                    SplashActivity.this.finish();
                }

                @Override
                public void onPrivacy1() {
                    PrivacyAgreeActivity.startIntent(SplashActivity.this);
                }

                @Override
                public void onPrivacy2() {
                    PrivacyAgreeActivity.startPrivacy(SplashActivity.this, coreManager.getConfig().privacyPolicyPrefix);
                }
            });
        }
        privacyInfoDialog.show();
    }

    @SuppressLint("NewApi")
    private void jump() {
        MyApplication.getInstance().initApp();

        if (isDestroyed()) {
            return;
        }
        int userStatus = LoginHelper.prepareUser(mContext, coreManager);
        Intent intent = new Intent();
        switch (userStatus) {
            case LoginHelper.STATUS_USER_FULL:
            case LoginHelper.STATUS_USER_NO_UPDATE:
            case LoginHelper.STATUS_USER_TOKEN_OVERDUE:
                boolean login = PreferenceUtils.getBoolean(this, Constants.LOGIN_CONFLICT, false);
                if (login) {// 登录冲突，退出app再次进入，跳转至历史登录界面
                    intent.setClass(mContext, SelectLoginModeActivity.class);
                } else {
                    intent.setClass(mContext, MainActivity.class);
                }
                break;
            case LoginHelper.STATUS_USER_SIMPLE_TELPHONE:
                intent.setClass(mContext, SelectLoginModeActivity.class);
                break;
            case LoginHelper.STATUS_NO_USER:
            default:
                stay();
                return;// must return
        }
        startActivity(intent);
        finish();
    }

    // 第一次进入，显示登录、注册按钮
    private void stay() {
        // 因为启动页有时会替换，无法做到按钮与图片的完美适配，干脆直接进入到登录页面
        startActivity(new Intent(mContext, SelectLoginModeActivity.class));
        finish();
    }


    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }
}
