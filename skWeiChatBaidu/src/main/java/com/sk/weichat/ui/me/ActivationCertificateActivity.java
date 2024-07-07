package com.sk.weichat.ui.me;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;

import com.sk.weichat.R;
import com.sk.weichat.event.ActivationEvent;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.base.BaseAuthActivity;
import com.sk.weichat.util.ClickUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.LoadingDialog;
import com.xuan.xuanhttplibrary.okhttp.Api;
import com.xuan.xuanhttplibrary.okhttp.RxScheduler;
import com.xuan.xuanhttplibrary.okhttp.result.BaseResult;

import java.util.HashMap;
import java.util.Map;

import autodispose2.AutoDispose;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import de.greenrobot.event.EventBus;
import io.reactivex.rxjava3.functions.Consumer;

public class ActivationCertificateActivity extends BaseAuthActivity implements View.OnClickListener {

    private EditText etNum;
    private TextView tvCheckType;
    private EditText etPassword;

    public static void start(Activity activity){
        Intent intent = new Intent(activity, ActivationCertificateActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);
        getSupportActionBar().hide();

        initView();
    }

    private void initView(){
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText("期权证书");

        etNum =findViewById(R.id.et_num);
        tvCheckType = findViewById(R.id.tv_checked_tyep);
        etPassword = findViewById(R.id.et_password);

        findViewById(R.id.btn_commit).setOnClickListener(this);
        findViewById(R.id.iv_title_left).setOnClickListener(this);
        tvCheckType.setOnClickListener(this);
    }

    private void activationVip(){
        showDialog();
        String userId = UserSp.getInstance(this).getUserId("");
        String account = etNum.getText().toString();
        String password = etPassword.getText().toString();
        if(TextUtils.isEmpty(account) || account.length() != 16){
            dimessDialog();
            ToastUtil.showToast(ActivationCertificateActivity.this, "请输入16位数编码");
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("account", account);
        if(passwordType == 0){
            if(TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 20){
                dimessDialog();
                ToastUtil.showToast(ActivationCertificateActivity.this, "请输入正确格式密码");
                return;
            }
            map.put("secretKey", password);
        }else{
            map.put("actionType", "nopass");
        }
        map.put("typeVersion",3);
        map.put("userId", Integer.parseInt(userId));
        long c = System.currentTimeMillis();
        Api.getInstance().getAppRetrofit().activateCert(map)
                .compose(RxScheduler.schedulerObservable())
                .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider
                        .from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(new Consumer<BaseResult>() {
                    @Override
                    public void accept(BaseResult result) throws Throwable {
                        dimessDialog();
                        if(result.getCode().equals("000000")){
                            MainActivity.isActivated = true;
                            ToastUtil.showToast(ActivationCertificateActivity.this, "激活成功");
                            EventBus.getDefault().post(new ActivationEvent());
                            ActivationCertificateActivity.this.finish();
                        }else{
                            ToastUtil.showToast(ActivationCertificateActivity.this, result.getMsg());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        dimessDialog();

                        Log.e("asdasdsadsadad",throwable.toString()+"\n time:"+(System.currentTimeMillis() - c));
                        ToastUtil.showNetError(ActivationCertificateActivity.this,throwable.toString());
                    }
                });
    }


    private LoadingDialog loadingDialog;

    public void showDialog(){
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.show();
    }

    public void dimessDialog(){
        if(loadingDialog != null){
            loadingDialog.dismiss();
        }
    }

    private int passwordType = 0;

    private void checkStyle(){
        String checkType = tvCheckType.getText().toString();
        if(checkType.equals("密码激活")){
            passwordType = 0;
            tvCheckType.setText("无密码激活");
            etPassword.setVisibility(View.VISIBLE);
        }else if(checkType.equals("无密码激活")){
            passwordType = 1;
            tvCheckType.setText("密码激活");
            etPassword.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if(ClickUtils.isFastClick(v.getId())) {
            if (v.getId() == R.id.iv_title_left) {
                this.finish();
            } else if (v.getId() == R.id.btn_commit) {
                activationVip();
            } else if(v.getId() == R.id.tv_checked_tyep){
                checkStyle();
            }
        }
    }
}