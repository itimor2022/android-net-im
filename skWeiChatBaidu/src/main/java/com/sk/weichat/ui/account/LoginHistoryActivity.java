package com.sk.weichat.ui.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.sk.weichat.AppConstant;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.Code;
import com.sk.weichat.bean.LoginRegisterResult;
import com.sk.weichat.bean.User;
import com.sk.weichat.bean.event.MessageLogin;
import com.sk.weichat.db.dao.UserDao;
import com.sk.weichat.helper.AvatarHelper;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.LoginHelper;
import com.sk.weichat.helper.LoginSecureHelper;
import com.sk.weichat.helper.PasswordHelper;
import com.sk.weichat.helper.PrivacySettingHelper;
import com.sk.weichat.helper.YeepayHelper;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.ActivityStack;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.login.AuthLoginActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.DeviceInfoUtil;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.util.secure.LoginPassword;
import com.sk.weichat.view.ChatImageView;
import com.sk.weichat.view.HorizontalListView;
import com.sk.weichat.view.VerifyDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 历史登陆界面
 */

public class LoginHistoryActivity extends BaseActivity implements View.OnClickListener {
    private ImageView mAvatarImgView;
    private TextView mNickNameTv;
    private EditText mPasswordEdit;
    private int mobilePrefix = 86;
    private User mLastLoginUser;
    private ImageView mImageCodeIv;
    private ImageView mRefreshIv;
    private Button mSendAgainBtn;
    private EditText mImageCodeEdit;
    private int reckonTime = 60;

    private Handler mReckonHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                mSendAgainBtn.setText(reckonTime + " " + "S");
                reckonTime--;
                if (reckonTime < 0) {
                    mReckonHandler.sendEmptyMessage(0x2);
                } else {
                    mReckonHandler.sendEmptyMessageDelayed(0x1, 1000);
                }
            } else if (msg.what == 0x2) {
                // 60秒结束
                mSendAgainBtn.setText(getString(R.string.send));
                mSendAgainBtn.setEnabled(true);
                reckonTime = 60;
            }
        }
    };

    public LoginHistoryActivity() {
        noLoginRequired();
    }

    private void start(String password, ObjectResult<LoginRegisterResult> result) {
        LoginHelper.setLoginUser(mContext, coreManager, phone, result.getData().getPassword(), result);

        LoginRegisterResult.Settings settings = result.getData().getSettings();
        MyApplication.getInstance().initPayPassword(result.getData().getUserId(), result.getData().getPayPassword());
        YeepayHelper.saveOpened(mContext, result.getData().getWalletUserNo() == 1);
        PrivacySettingHelper.setPrivacySettings(LoginHistoryActivity.this, settings);
        MyApplication.getInstance().initMulti();

        DataDownloadActivity.start(mContext, result.getData().getIsupdate(), password);
        finish();
    }

    public static void start(Context ctx) {
        Intent intent = new Intent(ctx, LoginHistoryActivity.class);
        // 清空activity栈，
        // 重建期间白屏，暂且放弃，
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_history);
        PreferenceUtils.putBoolean(this, Constants.LOGIN_CONFLICT, false);// 重置登录冲突记录
        String userId = UserSp.getInstance(this).getUserId("");
        mLastLoginUser = UserDao.getInstance().getUserByUserId(userId);
        if (!LoginHelper.isUserValidation(mLastLoginUser)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        initActionBar();
        initView();
        EventBusHelper.register(this);
    }

    @Override
    public void onBackPressed() {
        ActivityStack.getInstance().exit();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView tv1 = (TextView) findViewById(R.id.tv_title_left);
        TextView tv2 = (TextView) findViewById(R.id.tv_title_right);
        TextView tvContent = findViewById(R.id.tv_title_center);
        tvContent.setText(R.string.login);
        tv1.setText(R.string.app_name);
        tv2.setText(R.string.switch_account);
        tv2.setOnClickListener(v -> {
            Intent intent = new Intent(LoginHistoryActivity.this, AuthCodeActivity.class);
            startActivity(intent);
        });
    }

    private void initView() {
        mAvatarImgView = (ImageView) findViewById(R.id.avatar_img);
        mImageCodeIv = (ImageView) findViewById(R.id.image_iv);
        mSendAgainBtn = (Button) findViewById(R.id.send_again_btn);
        auth_code_edit = findViewById(R.id.auth_code_edit);
        mRefreshIv = (ImageView) findViewById(R.id.image_iv_refresh);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mImageCodeEdit = (EditText) findViewById(R.id.image_tv);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        PasswordHelper.bindPasswordEye(mPasswordEdit, findViewById(R.id.tbEye));
        mobilePrefix = PreferenceUtils.getInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        Button loginBtn, registerBtn, forgetPasswordBtn;
        loginBtn = (Button) findViewById(R.id.login_btn);
        ButtonColorChange.colorChange(this, loginBtn);
        loginBtn.setOnClickListener(this);
        registerBtn = (Button) findViewById(R.id.register_account_btn);
        registerBtn.setOnClickListener(this);
        if (coreManager.getConfig().isOpenRegister) {
            registerBtn.setVisibility(View.VISIBLE);
        } else {
            registerBtn.setVisibility(View.GONE);
        }
        forgetPasswordBtn = (Button) findViewById(R.id.forget_password_btn);
        if (coreManager.getConfig().registerUsername) {
            forgetPasswordBtn.setVisibility(View.GONE);
        } else {
            forgetPasswordBtn.setOnClickListener(this);
        }
        loginBtn.setText(getString(R.string.login));
        registerBtn.setText(getString(R.string.register_account));
        forgetPasswordBtn.setText(getString(R.string.forget_password));

        // 刷新图形码
        mRefreshIv.setOnClickListener(v -> {
            requestImageCode();
        });

        mSendAgainBtn.setOnClickListener(v -> {
            phone = mLastLoginUser.getTelephoneNoAreaCode();
            mImageCodeStr = mImageCodeEdit.getText().toString().trim();
            if (TextUtils.isEmpty(mImageCodeStr)) {
                ToastUtil.showToast(mContext, getString(R.string.tip_verification_code_empty));
                return;
            }
            // 验证手机号是否注册
            verifyPhoneIsRegistered(phone, mImageCodeStr);

        });

        AvatarHelper.getInstance().displayRoundAvatar(mLastLoginUser.getNickName(), mLastLoginUser.getUserId(), mAvatarImgView, true);
        mNickNameTv.setText(mLastLoginUser.getNickName());

    }

    private String phone;
    private String mImageCodeStr;

    /**
     * 验证手机是否注册
     */
    private void verifyPhoneIsRegistered(final String phoneStr, final String imageCodeStr) {
        verifyPhoneNumber(phoneStr, () -> requestAuthCode(phoneStr, imageCodeStr));
    }

    private void verifyPhoneNumber(String phoneNumber, final Runnable onSuccess) {
        Map<String, String> params = new HashMap<>();
        params.put("telephone", phoneNumber);
        params.put("areaCode", "" + mobilePrefix);
        params.put("verifyType", "1");
        HttpUtils.get().url(coreManager.getConfig().VERIFY_TELEPHONE)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result == null) {
                            ToastUtil.showToast(LoginHistoryActivity.this,
                                    R.string.data_exception);
                            return;
                        }

                        if (result.getResultCode() == 1) {
                            onSuccess.run();
                        } else {
                            requestImageCode();
                            // 手机号已经被注册
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(LoginHistoryActivity.this,
                                        result.getResultMsg());
                            } else {
                                ToastUtil.showToast(LoginHistoryActivity.this,
                                        R.string.tip_server_error);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(LoginHistoryActivity.this);
                    }
                });
    }

    /**
     * 请求验证码
     */
    private void requestAuthCode(String phoneStr, String imageCodeStr) {
        Map<String, String> params = new HashMap<>();
        String language = Locale.getDefault().getLanguage();
        params.put("language", language);
        params.put("areaCode", String.valueOf(mobilePrefix));
        params.put("telephone", phoneStr);
        params.put("imgCode", imageCodeStr);
        params.put("isRegister", String.valueOf(0));
        params.put("version", "1");

        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().SEND_AUTH_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<Code>(Code.class) {

                    @Override
                    public void onResponse(ObjectResult<Code> result) {
                        DialogHelper.dismissProgressDialog();
                        if (com.xuan.xuanhttplibrary.okhttp.result.Result.checkSuccess(mContext, result)) {
                            mSendAgainBtn.setEnabled(false);
                            // 开始倒计时
                            mReckonHandler.sendEmptyMessage(0x1);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    private EditText auth_code_edit;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(mContext, getString(R.string.phone_number_not_be_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(auth_code_edit.getText())) {
                    Toast.makeText(mContext, getString(R.string.tip_phone_number_verification_code_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                login();
                break;
            case R.id.register_account_btn:
                startActivity(new Intent(LoginHistoryActivity.this, RegisterActivity.class));
                break;
            case R.id.forget_password_btn:
                startActivity(new Intent(this, FindPwdActivity.class));
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageLogin message) {
        finish();
    }

    private void login() {
        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
        final String phoneNumber = mLastLoginUser.getTelephoneNoAreaCode();
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<>();
        params.put("xmppVersion", "1");
        // 附加信息
        params.put("model", DeviceInfoUtil.getModel());
        params.put("osVersion", DeviceInfoUtil.getOsVersion());
        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
        params.put("loginType", "1");//验证码登录

        // 地址信息
        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));

        if (MyApplication.IS_OPEN_CLUSTER) {// 服务端集群需要
            String area = PreferenceUtils.getString(this, AppConstant.EXTRA_CLUSTER_AREA);
            if (!TextUtils.isEmpty(area)) {
                params.put("area", area);
            }
        }

        LoginSecureHelper.smsLogin(
                this, coreManager, auth_code_edit.getText().toString().trim(), String.valueOf(mobilePrefix), phoneNumber,
                params,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
                }, result -> {
                    DialogHelper.dismissProgressDialog();
                    if (!com.xuan.xuanhttplibrary.okhttp.result.Result.checkSuccess(getApplicationContext(), result)) {
                        return;
                    }
                    if (!TextUtils.isEmpty(result.getData().getAuthKey())) {
                        DialogHelper.showMessageProgressDialog(mContext, getString(R.string.tip_need_auth_login));
                        CheckAuthLoginRunnable authLogin = new CheckAuthLoginRunnable(result.getData().getAuthKey());
                        waitAuth(authLogin);
                        return;
                    }
                    afterLogin(result);
                });
    }

//    private void login() {
//        PreferenceUtils.putInt(this, Constants.AREA_CODE_KEY, mobilePrefix);
//        String password = mPasswordEdit.getText().toString().trim();
//        if (TextUtils.isEmpty(password)) {
//            return;
//        }
//        final String digestPwd = LoginPassword.encodeMd5(password);
//
//        DialogHelper.showDefaulteMessageProgressDialog(this);
//        HashMap<String, String> params = new HashMap<>();
//        String phoneNumber = mLastLoginUser.getTelephoneNoAreaCode();
//        params.put("xmppVersion", "1");
//        // 附加信息
//        params.put("model", DeviceInfoUtil.getModel());
//        params.put("osVersion", DeviceInfoUtil.getOsVersion());
//        params.put("serial", DeviceInfoUtil.getDeviceId(mContext));
//        // 地址信息
//        double latitude = MyApplication.getInstance().getBdLocationHelper().getLatitude();
//        double longitude = MyApplication.getInstance().getBdLocationHelper().getLongitude();
//        if (latitude != 0)
//            params.put("latitude", String.valueOf(latitude));
//        if (longitude != 0)
//            params.put("longitude", String.valueOf(longitude));
//
//        if (MyApplication.IS_OPEN_CLUSTER) {// 服务端集群需要
//            String area = PreferenceUtils.getString(this, AppConstant.EXTRA_CLUSTER_AREA);
//            if (!TextUtils.isEmpty(area)) {
//                params.put("area", area);
//            }
//        }
//
//        LoginSecureHelper.secureLogin(
//                this, coreManager, String.valueOf(mobilePrefix), phoneNumber, password,
//                params,
//                t -> {
//                    DialogHelper.dismissProgressDialog();
//                    ToastUtil.showToast(this, this.getString(R.string.tip_login_secure_place_holder, t.getMessage()));
//                }, result -> {
//                    DialogHelper.dismissProgressDialog();
//                    if (!Result.checkSuccess(getApplicationContext(), result)) {
//                        return;
//                    }
//                    if (!TextUtils.isEmpty(result.getData().getAuthKey())) {
//                        DialogHelper.showMessageProgressDialog(mContext, getString(R.string.tip_need_auth_login));
//                        CheckAuthLoginRunnable authLogin = new CheckAuthLoginRunnable(result.getData().getAuthKey(), phoneNumber, digestPwd);
//                        waitAuth(authLogin);
//                        return;
//                    }
//                    afterLogin(result, phoneNumber, digestPwd);
//                });
//    }

//    private void afterLogin(ObjectResult<LoginRegisterResult> result, String phoneNumber, String digestPwd) {
//        boolean success = LoginHelper.setLoginUser(mContext, coreManager, phoneNumber, digestPwd, result);
//        if (success) {
//            LoginRegisterResult.Settings settings = result.getData().getSettings();
//            PrivacySettingHelper.setPrivacySettings(LoginHistoryActivity.this, settings);
//            MyApplication.getInstance().initPayPassword(result.getData().getUserId(), result.getData().getPayPassword());
//            YeepayHelper.saveOpened(mContext, result.getData().getWalletUserNo() == 1);
//            MyApplication.getInstance().initMulti();
//            DataDownloadActivity.start(mContext, result.getData().getIsupdate(), mPasswordEdit.getText().toString().trim());
//            finish();
//        } else {
//            // 登录失败
//            String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.login_failed) : result.getResultMsg();
//            ToastUtil.showToast(mContext, message);
//        }
//    }

    private VerifyDialog mVerifyDialog;

    private void afterLogin(ObjectResult<LoginRegisterResult> result) {
        if (MyApplication.IS_SUPPORT_SECURE_CHAT
                && result.getData().getIsSupportSecureChat() == 1) {// 新用户才需要，老用户不支持端到端加密，不需要
            // SecureFlag 短信验证码登录成功，将无法解密服务端返回的私钥，需要让用户输入密码解密
            mVerifyDialog = new VerifyDialog(mContext);
            mVerifyDialog.setVerifyClickListener(getString(R.string.input_password_to_decrypt_keys), new VerifyDialog.VerifyClickListener() {
                @Override
                public void cancel() {
                    mVerifyDialog.dismiss();
                    startActivity(new Intent(mContext, FindPwdActivity.class));
                }

                @Override
                public void send(String str) {
//                    checkPasswordWXAuthCodeLogin(str, result);
                }
            });
            mVerifyDialog.setDismiss(false);
            mVerifyDialog.setCancelButton(R.string.forget_password);
            mVerifyDialog.show();
        } else {
            start("", result);
        }
/*
        boolean success = LoginHelper.setLoginUser(mContext, coreManager, phone, result.getData().getPassword(), result);// 设置登陆用户信息
        if (success) {
            if (MyApplication.IS_SUPPORT_SECURE_CHAT
                    && result.getData().getIsSupportSecureChat() == 1) {// 新用户才需要，老用户不支持端到端加密，不需要
                // SecureFlag 短信验证码登录成功，将无法解密服务端返回的私钥，需要让用户输入密码解密
                mVerifyDialog = new VerifyDialog(mContext);
                mVerifyDialog.setVerifyClickListener(getString(R.string.input_password_to_decrypt_keys), new VerifyDialog.VerifyClickListener() {
                    @Override
                    public void cancel() {
                        mVerifyDialog.dismiss();
                        startActivity(new Intent(mContext, FindPwdActivity.class));
                    }

                    @Override
                    public void send(String str) {
                        checkPasswordWXAuthCodeLogin(str, result.getData());
                    }
                });
                mVerifyDialog.setDismiss(false);
                mVerifyDialog.setCancelButton(R.string.forget_password);
                mVerifyDialog.show();
            } else {
                start("", result.getData());
            }
        } else {
            // 登录失败
            String message = TextUtils.isEmpty(result.getResultMsg()) ? getString(R.string.login_failed) : result.getResultMsg();
            ToastUtil.showToast(mContext, message);
        }
*/
    }

    //    private void waitAuth(CheckAuthLoginRunnable authLogin) {
//        authLogin.waitAuthHandler.postDelayed(authLogin, 3000);
//    }
    private void waitAuth(CheckAuthLoginRunnable authLogin) {
        authLogin.waitAuthHandler.postDelayed(authLogin, 3000);
    }

    private class CheckAuthLoginRunnable implements Runnable {
        //        private final String phoneNumber;
//        private final String digestPwd;
//        private Handler waitAuthHandler = new Handler();
//        private int waitAuthTimes = 10;
//        private String authKey;
//
//        public CheckAuthLoginRunnable(String authKey, String phoneNumber, String digestPwd) {
//            this.authKey = authKey;
//            this.phoneNumber = phoneNumber;
//            this.digestPwd = digestPwd;
//        }
//
//        @Override
//        public void run() {
//            HttpUtils.get().url(coreManager.getConfig().CHECK_AUTH_LOGIN)
//                    .params("authKey", authKey)
//                    .build(true, true)
//                    .execute(new BaseCallback<LoginRegisterResult>(LoginRegisterResult.class) {
//                        @Override
//                        public void onResponse(ObjectResult<LoginRegisterResult> result) {
//                            if (Result.checkError(result, Result.CODE_AUTH_LOGIN_SCUESS)) {
//                                DialogHelper.dismissProgressDialog();
//                                login();
//                            } else if (Result.checkError(result, Result.CODE_AUTH_LOGIN_FAILED_1)) {
//                                waitAuth(CheckAuthLoginRunnable.this);
//                            } else {
//                                DialogHelper.dismissProgressDialog();
//                                if (!TextUtils.isEmpty(result.getResultMsg())) {
//                                    ToastUtil.showToast(mContext, result.getResultMsg());
//                                } else {
//                                    ToastUtil.showToast(mContext, R.string.tip_server_error);
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onError(Call call, Exception e) {
//                            DialogHelper.dismissProgressDialog();
//                            ToastUtil.showErrorNet(mContext);
//                        }
//                    });
//        }
        private Handler waitAuthHandler = new Handler();
        private int waitAuthTimes = 10;

        private String authKey;

        public CheckAuthLoginRunnable(String authKey) {
            this.authKey = authKey;
        }

        @Override
        public void run() {
            HttpUtils.get().url(coreManager.getConfig().CHECK_AUTH_LOGIN)
                    .params("authKey", authKey)
                    .build(true, true)
                    .execute(new BaseCallback<LoginRegisterResult>(LoginRegisterResult.class) {
                        @Override
                        public void onResponse(ObjectResult<LoginRegisterResult> result) {
                            if (com.xuan.xuanhttplibrary.okhttp.result.Result.checkError(result, com.xuan.xuanhttplibrary.okhttp.result.Result.CODE_AUTH_LOGIN_SCUESS)) {
                                DialogHelper.dismissProgressDialog();
                                login();
                            } else if (com.xuan.xuanhttplibrary.okhttp.result.Result.checkError(result, com.xuan.xuanhttplibrary.okhttp.result.Result.CODE_AUTH_LOGIN_FAILED_1)) {
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

    // ############################################################

    /**
     * 请求图形验证码
     */
    private void requestImageCode() {
        Map<String, String> params = new HashMap<>();
        params.put("telephone", mobilePrefix + mLastLoginUser.getTelephoneNoAreaCode());
        String url = HttpUtils.get().url(coreManager.getConfig().USER_GETCODE_IMAGE)
                .params(params)
                .buildUrl();
        Glide.with(mContext).load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        mImageCodeIv.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        Toast.makeText(LoginHistoryActivity.this, R.string.tip_verification_code_load_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
