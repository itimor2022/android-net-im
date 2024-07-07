package com.sk.weichat.ui.me.redpacket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.weichat.R;
import com.sk.weichat.bean.event.EventNotifyByTag;
import com.sk.weichat.bean.redpacket.Balance;
import com.sk.weichat.bean.redpacket.ScanRecharge;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.dialog.money.ScanRechargeBandDialog;
import com.sk.weichat.ui.dialog.money.ScanRechargeWxAlipayDialog;
import com.sk.weichat.ui.me.redpacket.scan.ScanRechargeActivity;
import com.sk.weichat.ui.me.redpacket.scan.ScanWithdrawActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

public class WxPayBlance extends BaseActivity {

    public static final String RSA_PRIVATE = "";
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;
    private TextView mBalanceTv;
    private TextView mRechargeTv;
    private TextView mWithdrawTv;
    private TextView mScanRechargeTv;
    private TextView mScanWithdrawTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay_blance);
        initActionBar();
        initView();
        // 初始化数据，获取平台usdt地址，用于向该平台地址充值
        initConfig();
        EventBusHelper.register(this);
    }

    @Override
    protected void onResume() {
        // todo 提现之后回到该界面，服务端待微信响应之后才会更新余额，此时调用刷新余额的方法获取到的可能还是之前的余额，另加一个EventBus来刷新吧
        super.onResume();
        initData();
    }

    private void initConfig() {
        Map<String, String> params = new HashMap<>();
        params.put("type", String.valueOf(4));// 1.微信 2.支付宝 3.银行卡 4 usdt

        HttpUtils.get().url(coreManager.getConfig().MANUAL_PAY_GET_RECEIVER_ACCOUNT)
                .params(params)
                .build()
                .execute(new BaseCallback<ScanRecharge>(ScanRecharge.class) {

                    @Override
                    public void onResponse(ObjectResult<ScanRecharge> result) {
                        if(result.getResultMsg() != null && result.getResultMsg().equals("后台未设置收款人")){

                        }else {
                            if (Result.checkSuccess(mContext, result)) {
                                ScanRecharge scanRecharge = result.getData();
                                coreManager.getConfig().rechargeusdtInfo = scanRecharge;
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(getString(R.string.my_purse));
        ImageView mImageView = findViewById(R.id.iv_title_right);
        mImageView.setImageDrawable(getResources().getDrawable(R.mipmap.navigation));
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 访问接口 获取记录
                Intent intent = new Intent(WxPayBlance.this, PaymentCenterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        mBalanceTv = (TextView) findViewById(R.id.myblance);
        mRechargeTv = (TextView) findViewById(R.id.chongzhi);
        mWithdrawTv = (TextView) findViewById(R.id.quxian);
        mScanRechargeTv = (TextView) findViewById(R.id.scan_recharge);
        mScanWithdrawTv = (TextView) findViewById(R.id.scan_withdraw);
        ButtonColorChange.rechargeChange(this, mWithdrawTv, R.drawable.recharge_icon);
        ButtonColorChange.rechargeChange(this, mScanWithdrawTv, R.drawable.recharge_icon);
        mWithdrawTv.setTextColor(SkinUtils.getSkin(this).getAccentColor());
        mScanWithdrawTv.setTextColor(SkinUtils.getSkin(this).getAccentColor());

        // 判断微信和支付宝, USDT是否都是关着的
//        boolean isClose = (!coreManager.getConfig().enableWxPay && !coreManager.getConfig().enableAliPay && !coreManager.getConfig().enableUsdtPay);
//        mWithdrawTv.setVisibility(isClose ? View.GONE : View.VISIBLE);
        // 屏蔽扫码充值
        /*if (coreManager.getConfig().isOpenManualPay) {
            mScanRechargeTv.setVisibility(View.VISIBLE);
            // 审核提现移至到提现内，如提现按钮被隐藏，审核提现则在外面显示
            mScanWithdrawTv.setVisibility(isClose ? View.VISIBLE : View.GONE);
        }*/

        mWithdrawTv.setOnClickListener(view -> {
            Intent intent = new Intent(WxPayBlance.this, QuXianActivity.class);
            startActivity(intent);
        });

        mScanWithdrawTv.setOnClickListener(view -> {
            Intent intent = new Intent(WxPayBlance.this, ScanWithdrawActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvPayPassword).setOnClickListener(v -> {
            Intent intent = new Intent(WxPayBlance.this, ChangePayPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void initData() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().RECHARGE_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        if (result.getResultCode() == 1 && result.getData() != null) {
                            DecimalFormat decimalFormat = new DecimalFormat("0.00");
                            Balance balance = result.getData();
                            coreManager.getSelf().setBalance(balance.getBalance());
                            mBalanceTv.setText(decimalFormat.format(((balance.getBalance()))) + " " + getString(R.string.yuan));
                            // 获取提现方式
                            coreManager.getConfig().withdrawStyleSwitch = balance.getWithdrawStyleSwitch();
                            coreManager.getConfig().withdrawRate = balance.getWithdrawRate();
                            coreManager.getConfig().usdtExchangeRate = balance.getUsdtExchangeRate();
                            coreManager.getConfig().payStyleSwitch = balance.getPayStyleSwitch();
                            togglePayStyle();
                            toggleWithDrawStyle();
                        } else {
                            ToastUtil.showErrorData(WxPayBlance.this);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(WxPayBlance.this);
                    }
                });
    }

    // 充值方式控制
    private void togglePayStyle() {
        /**
         * 扫码充值
         */
        if (coreManager.getConfig().payStyleSwitch == 0) {
            mRechargeTv.setVisibility(View.GONE);
            mScanRechargeTv.setVisibility(View.VISIBLE);
            ButtonColorChange.rechargeChange(this, mScanRechargeTv, R.drawable.chongzhi_icon);
            mScanRechargeTv.setOnClickListener(view -> {
                Intent intent = new Intent(WxPayBlance.this, ScanRechargeActivity.class);
                startActivity(intent);
            });
        } else if(coreManager.getConfig().payStyleSwitch == 1) {
            /**
             * 自动充值
             */
            mScanRechargeTv.setVisibility(View.GONE);
            mRechargeTv.setVisibility(View.VISIBLE);
            ButtonColorChange.rechargeChange(this, mRechargeTv, R.drawable.chongzhi_icon);
            mRechargeTv.setOnClickListener(view -> {
                Intent intent = new Intent(WxPayBlance.this, WxPayAdd.class);
                startActivity(intent);
            });
        }
    }

    // 提现方式控制
    private void toggleWithDrawStyle() {
        int withdrawStyleSwitch = coreManager.getConfig().withdrawStyleSwitch;
        if (withdrawStyleSwitch == 0) {
            // 0 手动提现
            mScanWithdrawTv.setVisibility(View.VISIBLE);
            mWithdrawTv.setVisibility(View.GONE);
        } else if (withdrawStyleSwitch == 1) {
            // 1 自动提现
            mScanWithdrawTv.setVisibility(View.GONE);
            mWithdrawTv.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventNotifyByTag message) {
        if (TextUtils.equals(message.tag, EventNotifyByTag.Withdraw)) {
            initData();
        }
    }
}
