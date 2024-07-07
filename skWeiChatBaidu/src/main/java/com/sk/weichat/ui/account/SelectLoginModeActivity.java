package com.sk.weichat.ui.account;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.rich.oauth.callback.PreLoginCallback;
import com.rich.oauth.callback.TokenCallback;
import com.rich.oauth.core.RichAuth;
import com.rich.oauth.core.UIConfigBuild;
import com.rich.oauth.util.RichLogUtil;
import com.sk.weichat.AppConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.EncryptedData;
import com.sk.weichat.bean.LoginRegisterResult;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.LoginSecureHelper;
import com.sk.weichat.helper.PrivacySettingHelper;
import com.sk.weichat.helper.YeepayHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.other.PrivacyAgreeActivity;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.PermissionUtil;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.secure.AES;
import com.sk.weichat.util.secure.MAC;
import com.sk.weichat.util.secure.MD5;
import com.sk.weichat.util.secure.Parameter;
import com.sk.weichat.view.PermissionExplainDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class SelectLoginModeActivity extends BaseActivity {

    private CheckBox checkBox;
    private final Map<String, Integer> permissionsMap = new LinkedHashMap<>();

    public SelectLoginModeActivity() {
        noLoginRequired();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);

        getSupportActionBar().hide();

        checkBox = findViewById(R.id.checkbox);

        initProtocol();

        setProtocolTextStyle(this, "登陆即同意运营商认证服务条款使用本机号码登陆");
        initUIConfig(LayoutInflater.from(this).inflate(R.layout.activity_one_click_login, null));

        findViewById(R.id.tvLoginOther).setOnClickListener(v -> {
            startActivity(new Intent(this, AuthCodeActivity.class));
        });
        findViewById(R.id.tvLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
//                if (checkBox.isChecked()) {
//                    preLogin(SelectLoginModeActivity.this);
//                } else {
//                    ToastUtil.showToast(getApplicationContext(), "请先仔细阅读并且同意协议");
//                }
            }
        });
        // 手机状态
        permissionsMap.put(Manifest.permission.READ_PHONE_STATE, R.string.permission_phone_status);
        // 照相
        permissionsMap.put(Manifest.permission.CAMERA, R.string.permission_photo);
        // 麦克风
        permissionsMap.put(Manifest.permission.RECORD_AUDIO, R.string.permission_microphone);
        // 存储权限
        permissionsMap.put(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_storage);
        permissionsMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage);
        permissionsMap.put(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.permission_location);
        permissionsMap.put(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_location);

        requestPermissions();
        macTest();
    }

    private void macTest() {
        HashMap<String,String> params = new HashMap<>();
        params.put("areaCode","86");
        params.put("version","1");
        params.put("language","zh");
        params.put("access_token","54ff3d79e80a4940beb43bde2d0760a9");
        params.put("imgCode","TKF9");
        params.put("isRegister","0");
        params.put("telephone","13901732954");

        byte[] key = MD5.encrypt(AppConfig.apiKey);
        String salt = "1677805209031";
        String macContent = AppConfig.apiKey + Parameter.joinValues(params) + salt;
        String mac = MAC.encodeBase64(macContent.getBytes(), key);
        System.out.println(mac);
    }

    private boolean requestPermissions() {
        return requestPermissions(permissionsMap.keySet().toArray(new String[]{}));
    }

    // 声明一个数组，用来存储所有需要动态申请的权限
    private static final int REQUEST_CODE = 0;

    private boolean requestPermissions(String... permissions) {
        List<String> deniedPermission = PermissionUtil.getDeniedPermissions(this, permissions);
        if (deniedPermission != null) {
            PermissionExplainDialog tip = getPermissionExplainDialog();
            tip.setPermissions(deniedPermission.toArray(new String[0]));
            tip.setOnConfirmListener(() -> {
                PermissionUtil.requestPermissions(this, REQUEST_CODE, permissions);
            });
            tip.show();
            return false;
        }
        return true;
    }


    // 复用请求权限的说明对话框，
    private PermissionExplainDialog permissionExplainDialog;

    private PermissionExplainDialog getPermissionExplainDialog() {
        if (permissionExplainDialog == null) {
            permissionExplainDialog = new PermissionExplainDialog(this);
        }
        return permissionExplainDialog;
    }

    // 一键登陆

    private UIConfigBuild mUiConfig;


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setProtocolTextStyle(Context context, String text) {
        SpannableString spannableString = new SpannableString(text);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getColor(R.color.color_999999));
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(12, true);
        spannableString.setSpan(colorSpan, 5, 14, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(sizeSpan, 5, 14, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//        view.bindProtocolView(spannableString);
    }

    public void loginByOperator(String token, String carrier) {
        HashMap<String, String> params = new HashMap<>();
        params.put("area", "");
        params.put("carrier", carrier);
        params.put("carrierToken", token);
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        params.put("loginType", "2");
        // 地址信息
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        params.put("latitude", "" + latitude);
        params.put("longitude", "" + longitude);
        String salt = String.valueOf(System.currentTimeMillis());
        String macContent = AppConfig.apiKey + Parameter.joinValues(params) + salt;
        Log.e("一键登录加密请求", "macContent >>> " + macContent);
        Log.e("一键登录加密请求", "salt >>> " + salt);
        byte[] key = MD5.encrypt(AppConfig.apiKey);
        String mac = MAC.encodeBase64(macContent.getBytes(), key);
        params.put("mac", mac);
        String data = AES.encryptBase64(JSON.toJSONString(params), MD5.encrypt(""));
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("data", data);
        requestParams.put("deviceId", "android");
        requestParams.put("salt", salt);
//        String requestUrl = "http://192.168.110.123:8095/user/oneClickLogin";
        String requestUrl = coreManager.getConfig().USER_ONE_CLICK_LOGIN;
        HttpUtils.get().url(requestUrl)
                .params(requestParams).build(true, true)
                .execute(new BaseCallback<EncryptedData>(EncryptedData.class) {
                    @Override
                    public void onResponse(ObjectResult<EncryptedData> result) {
                        DialogHelper.dismissProgressDialog();
                        ObjectResult<LoginRegisterResult> objectResult = new ObjectResult<>();
                        objectResult.setCurrentTime(result.getCurrentTime());
                        objectResult.setResultCode(result.getResultCode());
                        objectResult.setResultMsg(result.getResultMsg());
                        if (Result.checkSuccess(mContext, result, false) && result.getData() != null && result.getData().getData() != null) {
                            String realData = LoginSecureHelper.decodeLoginResult(MD5.encrypt(""), result.getData().getData());
                            if (realData != null) {
                                LoginRegisterResult realResult = JSON.parseObject(realData, LoginRegisterResult.class);
                                objectResult.setData(realResult);
                            }
                        }
                        if (!Result.checkSuccess(getApplicationContext(), objectResult)) {
                            ToastUtil.showToast(getApplicationContext(), objectResult.getResultMsg());
                            return;
                        }
                        String phoneNumber = objectResult.getData().getTelephone();
                        String digestPwd = objectResult.getData().getPassword();
                        if (!TextUtils.isEmpty(objectResult.getData().getAuthKey())) {
                            DialogHelper.showMessageProgressDialog(mContext, getString(R.string.tip_need_auth_login));
                            CheckAuthLoginRunnable authLogin = new CheckAuthLoginRunnable(objectResult.getData().getAuthKey(), phoneNumber, digestPwd);
                            waitAuth(authLogin);
                            return;
                        }
                        LoginHelper.setLoginUser(mContext, coreManager, objectResult.getData().getTelephone(), objectResult.getData().getPassword(), objectResult);

                        LoginRegisterResult.Settings settings = objectResult.getData().getSettings();
                        MyApplication.getInstance().initPayPassword(objectResult.getData().getUserId(), objectResult.getData().getPayPassword());
                        YeepayHelper.saveOpened(mContext, objectResult.getData().getWalletUserNo() == 1);
                        PrivacySettingHelper.setPrivacySettings(SelectLoginModeActivity.this, settings);
                        MyApplication.getInstance().initMulti();

                        DataDownloadActivity.start(mContext, objectResult.getData().getIsupdate(), "");
                        finish();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showToast(getApplicationContext(), "一键登录接口请求失败！！！！！！！" + e);
                    }
                });
    }

    ClickableSpan xySpan = new ClickableSpan() {
        @Override
        public void onClick(@NonNull View widget) {

        }

        @Override
        public void updateDrawState(TextPaint ds) {
            //点击事件去掉下划线
            ds.setUnderlineText(false);
        }
    };


    int i = 0;

    ClickableSpan ysSpan = new ClickableSpan() {
        @Override
        public void onClick(@NonNull View widget) {

        }

        @Override
        public void updateDrawState(TextPaint ds) {
            //点击事件去掉下划线
            ds.setUnderlineText(false);
        }
    };

    ClickableSpan[] spans = new ClickableSpan[]{xySpan, ysSpan};

    @RequiresApi(api = Build.VERSION_CODES.M)
    void getProtocolsItem(Context context, SpannableString spannableString, int start, int end) {
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getColor(R.color.color_999999));
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(16, true);
        spannableString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(sizeSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(spans[i], start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }

    public void preLogin(Context context) {
        RichAuth.getInstance().preLogin((Activity) context, new PreLoginCallback() {
            @Override
            public void onPreLoginSuccess() {
                RichLogUtil.e("预登录成功");
                ToastUtil.showToast(getApplicationContext(), "预登录成功");
                getLoginToken(SelectLoginModeActivity.this);
            }

            @Override
            public void onPreLoginFailure(String errorMsg) {
                RichLogUtil.e("预登录失败:" + errorMsg);
                ToastUtil.showToast(getApplicationContext(), "一键登录发生异常,请重新进入程序或重启手机再试，同时也可以选择其他方式进行登录。\n(" + errorMsg + ")");
            }
        });
    }

    /**
     * 一键登录
     */
    public void getLoginToken(Context context) {
        RichAuth.getInstance().login((Activity) context, new TokenCallback() {
            @Override
            public void onTokenSuccessResult(String token, String carrier) {

                DialogHelper.showMessageProgressDialog(SelectLoginModeActivity.this, "登录中,请稍后...");
//                getPhoneNumberFromTencent(token,carrier);
                loginByOperator(token, carrier);
            }

            @Override
            public void onTokenFailureResult(String s) {

            }

            @Override
            public void onOtherLoginWayResult() {

            }

            @Override
            public void onBackPressedListener() {

            }

            @Override
            public void onCheckboxChecked(Context context, JSONObject jsonObject) {

            }

            @Override
            public void onLoginClickComplete(Context context, JSONObject jsonObject) {

            }

            @Override
            public void onLoginClickStart(Context context, JSONObject jsonObject) {

            }
        }, mUiConfig);
    }

    /**
     * 一键登录UI设置
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initUIConfig(View view) {
        //一键登录
        UIConfigBuild.Builder configBuild = new UIConfigBuild.Builder();

        configBuild.setAuthContentView(view);
        configBuild.setStatusBar(Color.TRANSPARENT, true);
        configBuild.setFitsSystemWindows(false);

        // 号码栏设置
        configBuild.setNumberColor(getApplicationContext().getColor(R.color.color_2A2A2A));
        configBuild.setNumberSize(36, true);
        configBuild.setNumberOffsetX(0);
        configBuild.setNumFieldOffsetY(230);
        // 按钮设置
//        configBuild.setLoginBtnHight(0);
//        configBuild.setLoginBtnWidth(0);

        configBuild.setLoginBtnBg(R.drawable.selector_button_login);
        configBuild.setLoginBtnText("确认登陆");
        configBuild.setLoginBtnTextSize(17);
        configBuild.setLoginBtnTextBold(false);
        configBuild.setLoginBtnWidth(280);
        configBuild.setLoginBtnHight(52);
        configBuild.setLogBtnMarginLeft(40);
        configBuild.setLogBtnMarginRight(40);
        configBuild.setLogBtnOffsetY(370);
        configBuild.setLoginBtnTextColor(Color.WHITE);

        configBuild.setProtocol("统一认证", "https://www.baidu.com/");
        configBuild.setSecondProtocol("腾讯sdk", "https://www.baidu.com/");
        // 是否设置书名号
        configBuild.setPrivacyBookSymbol(true);
        //设置协议勾选框+协议文本的抖动动画效果，默认无抖动。
        configBuild.setPrivacyAnimationBoolean(true);
        configBuild.setPrivacyOffsetY(100);
//
        // 协议字体颜色，第一个参数为协议颜色，第二个为协议其他字体颜色
        configBuild.setPrivacyColor(0xff0085d0, 0xff666666);
        //设置隐私条款相对于标题栏下边缘y偏移
        configBuild.setPrivacyOffsetY(30);
        //设置隐私条款相对于底部y偏移
        configBuild.setPrivacyOffsetY_B(35);
        //设置隐私条款距离手机左右边缘的边距
        configBuild.setPrivacyMarginLeft(20);
        //设置隐私条款距离手机左右边缘的边距
        configBuild.setPrivacyMarginRight(30);
        //设置隐私条款的字体大小
        configBuild.setPrivacyTextSize(12);
        //设置隐私条款的字体基本颜色：灰色
        configBuild.setClauseBaseColor(0xff666666);
        //设置隐私条款的字体突出颜色：蓝色
        configBuild.setClauseColor(0xff0085d0);
        //设置隐私条款的字体是否居中
        configBuild.setIsGravityCenter(false);


//
//        //设置隐私条款勾选框是否居中，0表示居上，1为居中
//        configBuild.setCheckBoxLocation(0);
//
//        //设置隐私条款勾选框宽度,单位：dp
//        configBuild.setCheckBoxImageWidth(12);
//        //设置隐私条款勾选框高度,单位：dp
//        configBuild.setCheckBoxImageheight(12);
//
//        configBuild.setPrivacyNavBgColor(0xff0000ff);
//        //设置隐私协议页面服务条款标题字体颜色
//        configBuild.setPrivacyNavTextColor(0xff0000ff);
//        //设置隐私协议页面服务条款标题字体大小
//        configBuild.setPrivacyNavTextSize(15);
//        // 设置隐私协议页面标题栏
//        configBuild.setPrivacyNavReturnBackClauseLayoutResID(R.layout.title_layout);
//        //  7、登录页面进出场动画
//        configBuild.setAuthPageActIn("in_activity", "out_activity");  // 设置进入一键登录页面进场动画,in_activity,out_activity为资源文件中anim动画文件名字
//        configBuild.setAuthPageActOut("in_activity", "out_activity");  // 设置进入一键登录页面退出动画
//        //  8、其他，如授权页获取token后是否主动关闭授权页,默认为true；
        configBuild.setAutoClosAuthPage(true);
//        //授权页语言切换 0.中文简体 1.中文繁体 2.英文
//        configBuild.setAppLanguageType(0);
        Context appContext = getApplicationContext();
        Button btnLogin = view.findViewById(R.id.btn_login);
        btnLogin.setVisibility(View.INVISIBLE);

        TextView btnOtherLogin = view.findViewById(R.id.btn_other_login);
        btnOtherLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectLoginModeActivity.this, AuthCodeActivity.class));
            }
        });

        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mUiConfig = configBuild.build();
    }

    private class CheckAuthLoginRunnable implements Runnable {
        private final String phoneNumber;
        private final String digestPwd;
        private Handler waitAuthHandler = new Handler();
        private int waitAuthTimes = 10;
        private String authKey;

        public CheckAuthLoginRunnable(String authKey, String phoneNumber, String digestPwd) {
            this.authKey = authKey;
            this.phoneNumber = phoneNumber;
            this.digestPwd = digestPwd;
        }

        @Override
        public void run() {
            HttpUtils.get().url(coreManager.getConfig().CHECK_AUTH_LOGIN)
                    .params("authKey", authKey)
                    .build(true, true)
                    .execute(new BaseCallback<LoginRegisterResult>(LoginRegisterResult.class) {
                        @Override
                        public void onResponse(ObjectResult<LoginRegisterResult> result) {
                            if (Result.checkError(result, Result.CODE_AUTH_LOGIN_SCUESS)) {
                                DialogHelper.dismissProgressDialog();
                            } else if (Result.checkError(result, Result.CODE_AUTH_LOGIN_FAILED_1)) {
                                waitAuth(CheckAuthLoginRunnable.this);
                            } else {
                                DialogHelper.dismissProgressDialog();
                                if (!TextUtils.isEmpty(result.getResultMsg())) {
                                    ToastUtil.showToast(mContext, result.getResultMsg());
                                } else {
                                    ToastUtil.showToast(mContext, R.string.tip_server_error);
                                }
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            DialogHelper.dismissProgressDialog();
                            ToastUtil.showErrorNet(mContext);
                        }
                    });
        }
    }

    private void initProtocol() {
        TextView tvProtocol = findViewById(R.id.tv_protocol);
        String protocol = "已阅读并同意《用户协议》《隐私政策》和《运营商名称统一认证服务条款》";
        //需要显示的字串
        SpannableString spannedString = new SpannableString(protocol);
        //设置点击字体颜色
        ForegroundColorSpan colorSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.blue));
        spannedString.setSpan(colorSpan1, 6, 12, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ForegroundColorSpan colorSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.blue));
        spannedString.setSpan(colorSpan2, 12, 18, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                PrivacyAgreeActivity.startIntent(SelectLoginModeActivity.this);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                //点击事件去掉下划线
                ds.setUnderlineText(false);
            }
        };
        spannedString.setSpan(clickableSpan1, 6, 12, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                PrivacyAgreeActivity.startPrivacy(SelectLoginModeActivity.this, coreManager.getConfig().privacyPolicyPrefix);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                //点击事件去掉下划线
                ds.setUnderlineText(false);
            }
        };
        spannedString.setSpan(clickableSpan2, 12, 18, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        tvProtocol.setMovementMethod(LinkMovementMethod.getInstance());
        tvProtocol.setHighlightColor(Color.TRANSPARENT);
        tvProtocol.setText(spannedString);
    }

    private void waitAuth(CheckAuthLoginRunnable authLogin) {
        authLogin.waitAuthHandler.postDelayed(authLogin, 3000);
    }

}
