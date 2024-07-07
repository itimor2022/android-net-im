package com.sk.weichat.ui.me;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;

import com.sk.weichat.R;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.BaseActivity;
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
import io.reactivex.rxjava3.functions.Consumer;

/**
 * @Author Roken
 * @Time 2022/9/2
 * @Describe 辅助激活
 */
public class AuxiliaryActivity extends BaseActivity implements View.OnClickListener {

    private EditText etNum;
    private EditText etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auxiliary);
        getSupportActionBar().hide();

        initView();
    }

    private void initView(){
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText("辅助激活");

        etNum =findViewById(R.id.et_num);
        etPhone = findViewById(R.id.et_phone);

        findViewById(R.id.iv_title_left).setOnClickListener(this);
        findViewById(R.id.btn_commit).setOnClickListener(this);
    }

    private void activationVip(){
        showDialog();
        String userId = UserSp.getInstance(this).getUserId("");
        String account = etNum.getText().toString();
        String phone = etPhone.getText().toString();
        if(TextUtils.isEmpty(account) || account.length() != 16){
            dimessDialog();
            ToastUtil.showToast(AuxiliaryActivity.this, "请输入16位数vip编码");
            return;
        }
        if(TextUtils.isEmpty(phone) && phone.length() < 11 ){
            dimessDialog();
            ToastUtil.showToast(AuxiliaryActivity.this, "请输入正确手机号码");
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("account", account);
        map.put("actionType", "nopass");
        map.put("telephone", phone);
        map.put("typeVersion",3);
        map.put("userId", Integer.parseInt(userId));
        Api.getInstance().getAppRetrofit().activateVip(map)
                .compose(RxScheduler.schedulerObservable())
                .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider
                        .from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(new Consumer<BaseResult>() {
                    @Override
                    public void accept(BaseResult result) throws Throwable {
                        dimessDialog();
                        if(result.getCode().equals("000000")){
                            ToastUtil.showToast(AuxiliaryActivity.this, "激活成功");
                            AuxiliaryActivity.this.finish();
                        }else{
                            ToastUtil.showToast(AuxiliaryActivity.this, result.getMsg());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        dimessDialog();
                        ToastUtil.showNetError(AuxiliaryActivity.this);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if(ClickUtils.isFastClick(v.getId())) {
            if (v.getId() == R.id.iv_title_left) {
                this.finish();
            } else if (v.getId() == R.id.btn_commit) {
                activationVip();
            }
        }
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
}
