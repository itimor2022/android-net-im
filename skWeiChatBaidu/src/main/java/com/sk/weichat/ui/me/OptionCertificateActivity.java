package com.sk.weichat.ui.me;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.sk.weichat.R;
import com.sk.weichat.adapter.VipCardAdapter;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.util.ClickUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.LoadingDialog;
import com.xuan.xuanhttplibrary.okhttp.Api;
import com.xuan.xuanhttplibrary.okhttp.RxScheduler;
import com.xuan.xuanhttplibrary.okhttp.result.BaseResult;
import com.xuan.xuanhttplibrary.okhttp.result.response.VipHas;
import com.xuan.xuanhttplibrary.okhttp.result.response.VipInfos;

import java.util.HashMap;
import java.util.Map;

import autodispose2.AutoDispose;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * @Author Roken
 * @Time 2022/8/18
 * @Describe 期权证书
 */
public class OptionCertificateActivity extends BaseActivity implements View.OnClickListener {

    private ImageView ivImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_certificate);
        getSupportActionBar().hide();

        initView();
    }

    private void initView(){
        ivImg = findViewById(R.id.iv_certificate);

        findViewById(R.id.iv_title_left).setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText("期权证书");

    }

    @Override
    public void onClick(View v) {
        if(ClickUtils.isFastClick(v.getId())) {
            if (v.getId() == R.id.iv_title_left) {
                this.finish();
            }
        }
    }

    private void getCertificateInfo() {
//        showDialog();
//        Map<String, String> map = new HashMap<>();
//        map.put("userId", userId);
//        Api.getInstance().getAppRetrofit().getVipInfo(map)
//                .compose(RxScheduler.schedulerObservable())
//                .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider
//                        .from(this, Lifecycle.Event.ON_DESTROY)))
//                .subscribe(new Consumer<BaseResult<VipInfos>>() {
//                    @Override
//                    public void accept(BaseResult<VipInfos> baseResult) throws Throwable {
//                        dimessDialog();
//                        if (baseResult.getCode().equals("000000")) {
//                            adapter = new VipCardAdapter(VipActivity.this, baseResult.getData().getItems());
//                            rv.setAdapter(adapter);
//                            rv.setLayoutManager(new LinearLayoutManager(VipActivity.this) {
//                                @Override
//                                public boolean canScrollVertically() {
//                                    return false;
//                                }
//                            });
//                            findViewById(R.id.tv_add).setVisibility(baseResult.getData().isActivateViewFlag() ? View.VISIBLE : View.GONE);
//
////                                setVip(baseResult.getData());
//                        } else {
//                            ToastUtil.showToast(VipActivity.this, baseResult.getMsg());
//                        }
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Throwable {
//                        dimessDialog();
//                        ToastUtil.showNetError(VipActivity.this);
//                    }
//                });
    }

    private TextView tv_vip_code;
    private TextView tv_vip_serial;
    private TextView tv_address;


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
