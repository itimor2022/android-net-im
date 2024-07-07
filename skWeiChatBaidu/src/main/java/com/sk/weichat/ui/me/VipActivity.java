package com.sk.weichat.ui.me;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.adapter.VipCardAdapter;
import com.sk.weichat.sp.UserSp;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.dialog.DialogHint;
import com.sk.weichat.util.ClickUtils;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.LoadingDialog;
import com.xuan.xuanhttplibrary.okhttp.Api;
import com.xuan.xuanhttplibrary.okhttp.RxScheduler;
import com.xuan.xuanhttplibrary.okhttp.result.BaseResult;
import com.xuan.xuanhttplibrary.okhttp.result.response.VipInfo;
import com.xuan.xuanhttplibrary.okhttp.result.response.VipInfos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import autodispose2.AutoDispose;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * @Author Roken
 * @Time 2022/8/18
 * @Describe 我的会员
 */
public class VipActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);
        getSupportActionBar().hide();

        initView();
    }

    private RecyclerView rv;
    private VipCardAdapter adapter;

    private void initView() {


        rv = findViewById(R.id.rv_vip_card);
        findViewById(R.id.iv_title_left).setOnClickListener(this);

        findViewById(R.id.tv_add).setOnClickListener(this);

        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText("我的会员");
        getVip();

    }

    private void getVip() {
        String userId = UserSp.getInstance(this).getUserId("");
        if (!TextUtils.isEmpty(userId)) {
            showDialog();
            Map<String, String> map = new HashMap<>();
            map.put("typeVersion", 3 + "");
            map.put("userId", userId);
            Api.getInstance().getAppRetrofit().getVipInfo(map)
                    .compose(RxScheduler.schedulerObservable())
                    .to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider
                            .from(this, Lifecycle.Event.ON_DESTROY)))
                    .subscribe(new Consumer<BaseResult<VipInfos>>() {
                        @Override
                        public void accept(BaseResult<VipInfos> baseResult) throws Throwable {
                            dimessDialog();
                            if (baseResult.getCode().equals("000000")) {
                                adapter = new VipCardAdapter(VipActivity.this, baseResult.getData().getItems());
                                rv.setAdapter(adapter);
                                rv.setLayoutManager(new LinearLayoutManager(VipActivity.this) {
                                    @Override
                                    public boolean canScrollVertically() {
                                        return false;
                                    }
                                });
                                findViewById(R.id.tv_add).setVisibility(baseResult.getData().isActivateViewFlag() ? View.VISIBLE : View.GONE);

//                                setVip(baseResult.getData());
                            } else {
                                ToastUtil.showToast(VipActivity.this, baseResult.getMsg());
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Throwable {
                            dimessDialog();
                            ToastUtil.showNetError(VipActivity.this);
                        }
                    });
        }
    }

    private TextView tv_vip_code;
    private TextView tv_vip_serial;
    private TextView tv_address;

    private void setVip(VipInfo vipInfo) {
        if (vipInfo != null) {
            tv_vip_code = findViewById(R.id.tv_vip_code);
            tv_vip_code.setText(vipInfo.getNumber());
            tv_vip_serial = findViewById(R.id.tv_vip_serial);
            tv_vip_serial.setText("序列号 " + vipInfo.getSerial());
            tv_address = findViewById(R.id.tv_address);
            tv_address.setText(vipInfo.getAddress());
        }
    }

    private void showDialog(String hint) {

        DialogHint dialogHint = new DialogHint(this, hint);
        dialogHint.setOnCloseListener(new DialogHint.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm) {
                if (confirm) {
//                    if (!MainActivity.isCertified) {
//                        CertificateActivity.start(this, false);
//                    } else {
//                        ActivationActivity.start(getActivity());
//                    }
                    ActivationActivity.start(VipActivity.this);

                    dialogHint.dismiss();
                    finish();
                    return;
                }

                dialogHint.dismiss();
            }
        });
        dialogHint.show();
    }


    private LoadingDialog loadingDialog;

    public void showDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.show();
    }

    public void dimessDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }


    @Override
    public void onClick(View v) {
        if (ClickUtils.isFastClick(v.getId())) {
            if (v.getId() == R.id.iv_title_left) {
                this.finish();
            } else if (v.getId() == R.id.tv_add) {
                showDialog("新增一张会员，开始前往激活");
            }
        }
    }
}
