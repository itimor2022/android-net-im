package com.sk.weichat.ui.me;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;

import com.sk.weichat.R;
import com.sk.weichat.event.CertificateEvent;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.MainActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.base.BaseAuthActivity;
import com.sk.weichat.ui.dialog.DialogCertificate;
import com.sk.weichat.util.ClickUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.ClearEditText;
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

/**
 * @Author Roken
 * @Time 2022/8/26
 * @Describe 描述
 */
public class CertificateActivity extends BaseAuthActivity implements View.OnClickListener {

    private static final String IS_CERTIFICATE = "isCertificate";

    public static void start(Activity activity, boolean certificate){
        Intent intent = new Intent(activity, CertificateActivity.class);
        intent.putExtra(IS_CERTIFICATE, certificate);
        activity.startActivity(intent);
    }


    private ClearEditText etName;

    private ClearEditText etIdNum;

    private boolean isCertificate =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);
        getSupportActionBar().hide();

        initView();
    }

    private void initView(){
        etName =  findViewById(R.id.et_name);
        etIdNum =findViewById(R.id.et_idcard);

        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText("实名认证");

        findViewById(R.id.btn_commit).setOnClickListener(this);

        findViewById(R.id.iv_title_left).setOnClickListener(this);

        isCertificate = getIntent().getBooleanExtra(IS_CERTIFICATE, false);
        if(isCertificate){
            findViewById(R.id.rl_input).setVisibility(View.GONE);
            findViewById(R.id.tv_certificate_text).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.rl_input).setVisibility(View.VISIBLE);
            findViewById(R.id.tv_certificate_text).setVisibility(View.GONE);
        }
    }

    private void certificateResume(){
        String name = etName.getText().toString();
        String idcard = etIdNum.getText().toString();
        if(TextUtils.isEmpty(name)){
            dimessDialog();
            ToastUtil.showToast(CertificateActivity.this, "请输入姓名");
            return;
        }
        if(TextUtils.isEmpty(idcard)){
            dimessDialog();
            ToastUtil.showToast(CertificateActivity.this, "请输入正确的身份证号码");
            return;
        }
        showCertificateDialog(name, idcard);
    }

    private DialogCertificate dialog;

    private void showCertificateDialog(String name, String no){
        dialog = new DialogCertificate(this, "姓名："+ name, "身份证:"+no)
                .setTitle("提示");
        dialog.setOnCloseListener(new DialogCertificate.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm) {
                if(confirm){
                    certificate();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void certificate(){
        showDialog();
        String userId = UserSp.getInstance(this).getUserId("");
        String name = etName.getText().toString();
        String idcard = etIdNum.getText().toString();
        if(TextUtils.isEmpty(name)){
            dimessDialog();
            ToastUtil.showToast(CertificateActivity.this, "请输入姓名");
            return;
        }
        if(TextUtils.isEmpty(idcard)){
            dimessDialog();
            ToastUtil.showToast(CertificateActivity.this, "请输入正确的身份证号码");
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("idcard", idcard);
        map.put("truename", name);
        map.put("userId", userId);
        Api.getInstance().getAppRetrofit().identityIdCard(map)
                .compose(RxScheduler.schedulerObservable())
                .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider
                        .from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(new Consumer<BaseResult>() {
                    @Override
                    public void accept(BaseResult result) throws Throwable {
                        dimessDialog();
                        if(result.getCode().equals("000000")){
                            ToastUtil.showToast(CertificateActivity.this, "认证成功");
                            ActivationActivity.start(CertificateActivity.this);
                            EventBus.getDefault().post(new CertificateEvent());
                            MainActivity.isCertified = true;
                            CertificateActivity.this.finish();
                        }else{
                            ToastUtil.showToast(CertificateActivity.this, result.getMsg());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        dimessDialog();
                        ToastUtil.showNetError(CertificateActivity.this);
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


    @Override
    public void onClick(View v) {
        if(ClickUtils.isFastClick(v.getId())) {
            if (v.getId() == R.id.iv_title_left) {
                this.finish();
            } else if (v.getId() == R.id.btn_commit) {
                certificateResume();
            }
        }
    }
}
