package com.sk.weichat.ui.me.redpacket;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.sk.weichat.R;
import com.sk.weichat.bean.event.EventPaySuccess;
import com.sk.weichat.bean.redpacket.Balance;
import com.sk.weichat.bean.redpacket.ScanRecharge;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.dialog.money.RechargeUSDTDialog;
import com.sk.weichat.ui.dialog.money.ScanRechargeBandDialog;
import com.sk.weichat.ui.dialog.money.ScanRechargeWxAlipayDialog;
import com.sk.weichat.ui.me.redpacket.alipay.AlipayHelper;
import com.sk.weichat.util.Constants;
import com.sk.weichat.util.EventBusHelper;
import com.sk.weichat.util.SkinUtils;
import com.sk.weichat.util.ToastUtil;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import okhttp3.Call;

/**
 * 充值
 */
public class WxPayAdd extends BaseActivity {
    private IWXAPI api;

    private List<BigDecimal> mRechargeList = new ArrayList<>();
    private List<CheckedTextView> mRechargeMoneyViewList = new ArrayList<>();

    private EditText mSelectMoneyTv;
    private int mSelectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay_add);

        api = WXAPIFactory.createWXAPI(this, Constants.VX_APP_ID, false);
        api.registerApp(Constants.VX_APP_ID);

        initActionBar();
        initData();
        initView();

        EventBusHelper.register(this);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventPaySuccess message) {
        finish();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.recharge));
    }

    private void initData() {
        mRechargeList.add(new BigDecimal("10"));
        mRechargeList.add(new BigDecimal("20"));
        mRechargeList.add(new BigDecimal("50"));
        mRechargeList.add(new BigDecimal("100"));
        mRechargeList.add(new BigDecimal("200"));
        mRechargeList.add(new BigDecimal("500"));
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        TableLayout tableLayout = findViewById(R.id.tableLayoutRechargeMoney);
        SkinUtils.Skin skin = SkinUtils.getSkin(this);
        ColorStateList highlightColorState = skin.getHighlightColorState();
        View.OnClickListener onMoneyClickListener = v -> {
            mSelectedPosition = -1;
            for (int i = 0, mRechargeMoneyViewListSize = mRechargeMoneyViewList.size(); i < mRechargeMoneyViewListSize; i++) {
                CheckedTextView textView = mRechargeMoneyViewList.get(i);
                if (textView == v) {
                    mSelectedPosition = i;
                    mSelectMoneyTv.setText(mRechargeList.get(i).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
                    textView.setChecked(true);
                } else {
                    textView.setChecked(false);
                }
            }
        };
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
            for (int k = 0; k < tableRow.getChildCount(); k++) {
                CheckedTextView tvMoney = tableRow.getChildAt(k).findViewById(R.id.tvRechargeMoney);
                tvMoney.setOnClickListener(onMoneyClickListener);
                tvMoney.setTextColor(highlightColorState);
                ViewCompat.setBackgroundTintList(tvMoney, ColorStateList.valueOf(skin.getAccentColor()));
                int index = i * tableRow.getChildCount() + k;
                tvMoney.setText(mRechargeList.get(index).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + getString(R.string.yuan));
                mRechargeMoneyViewList.add(tvMoney);
            }
        }

        mSelectMoneyTv = findViewById(R.id.select_money_tv);
        mSelectMoneyTv.setTextColor(skin.getAccentColor());
        mSelectMoneyTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        mSelectMoneyTv.setText(s);
                        mSelectMoneyTv.setSelection(s.length());
                    }
                }

                if (!TextUtils.isEmpty(s) && s.toString().trim().substring(0, 1).equals(".")) {
                    s = "0" + s;
                    mSelectMoneyTv.setText(s);
                    mSelectMoneyTv.setSelection(1);
                }

                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        mSelectMoneyTv.setText(s.subSequence(0, 1));
                        mSelectMoneyTv.setSelection(1);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
/*
                try {
                    BigDecimal money = new BigDecimal(s.toString());
                    if (money.scale() > 2) {
                        mSelectMoneyTv.setText(money.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
                        return;
                    }
                } catch (Exception ignored) {
                    // 就算TextUtils.isEmpty判断了s不空，还是可能NumberFormatException: For input string: ""
                    // 看着像是存在异步修改，多线程冲突，但是没找到，
                }
*/
                if (!TextUtils.isEmpty(s)) {
                    mSelectMoneyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                    mSelectMoneyTv.setHint(null);
                } else {
                    // invisible占着高度，
                    mSelectMoneyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    mSelectMoneyTv.setHint(R.string.need_input_money);
                }
            }
        });

        findViewById(R.id.recharge_wechat).setVisibility(coreManager.getConfig().enableWxPay ? View.VISIBLE : View.GONE);
        findViewById(R.id.recharge_wechat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (api.getWXAppSupportAPI() < Build.PAY_SUPPORTED_SDK_INT) {
                    Toast.makeText(getApplicationContext(), R.string.tip_no_wechat, Toast.LENGTH_SHORT).show();
                } else {
                    recharge(getCurrentMoney());
                }
            }
        });

        findViewById(R.id.recharge_alipay).setVisibility(coreManager.getConfig().enableAliPay ? View.VISIBLE : View.GONE);
        findViewById(R.id.recharge_alipay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlipayHelper.recharge(WxPayAdd.this, coreManager, getCurrentMoney());
            }
        });
        // USDT充值 enableUSDT
        findViewById(R.id.recharge_usdt).setVisibility(coreManager.getConfig().enableUsdtPay ? View.VISIBLE : View.GONE);
        findViewById(R.id.recharge_usdt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUSDTRecharge(getCurrentMoney());
            }
        });
    }

    private  void showUSDTRecharge (String chargeMoney) {
        if (chargeMoney == null || TextUtils.isEmpty(chargeMoney)) {
            Toast.makeText(getApplicationContext(), R.string.tip_recharge_usdt_required_input_amount, Toast.LENGTH_SHORT).show();
            return;
        }
        ScanRecharge rechargeusdtInfo = coreManager.getConfig().rechargeusdtInfo;
        String rechargeusdtAddress = "";
        if (rechargeusdtInfo != null) {
            rechargeusdtAddress = rechargeusdtInfo.getPayNo();
        }
        RechargeUSDTDialog rechargeUSDTDialog = new RechargeUSDTDialog(mContext, rechargeusdtAddress, chargeMoney);
        rechargeUSDTDialog.show();
    }

    private String getCurrentMoney() {
        if (TextUtils.isEmpty(mSelectMoneyTv.getText())) {
            return "0";
        }
        return new BigDecimal(mSelectMoneyTv.getText().toString()).stripTrailingZeros().toPlainString();
    }

    private void recharge(String money) {// 调用服务端接口，由服务端统一下单
        DialogHelper.showDefaulteMessageProgressDialog(this);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("price", money);
        params.put("payType", "2");// 支付方式 1.支付宝 2.微信

        HttpUtils.get().url(coreManager.getConfig().VX_RECHARGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Balance>(Balance.class) {

                    @Override
                    public void onResponse(ObjectResult<Balance> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            PayReq req = new PayReq();
                            req.appId = result.getData().getAppId();
                            req.partnerId = result.getData().getPartnerId();
                            req.prepayId = result.getData().getPrepayId();
                            req.packageValue = "Sign=WXPay";
                            req.nonceStr = result.getData().getNonceStr();
                            req.timeStamp = result.getData().getTimeStamp();
                            req.sign = result.getData().getSign();
                            api.sendReq(req);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(WxPayAdd.this);
                    }
                });
    }
}
