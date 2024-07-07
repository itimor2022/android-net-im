package com.sk.weichat.ui.dialog.money;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.redpacket.ScanRecharge;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.util.ScreenUtil;
import com.sk.weichat.util.ToastUtil;
import com.sk.weichat.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 银行卡充值弹窗
 */
public class RechargeUSDTDialog extends Dialog {
    private ImageView mCloseIv;
    private TextView mTipTv;
    private TextView mConfirmTv, mCancelTv;
    private TextView mTv1, mTv3;
    private ImageView mIv1, mIv2, mIv3;
    private EditText mTv2;
    private Spinner mSp2;

    private Context mContext;
    private String name, card, bandName;
    private String money;

    // usdt地址
    private String usdtAddress;
    private EditText etAdress;
    private EditText paymentAdress;

    private ArrayAdapter<CharSequence> usdtTypeAdapter;

    public RechargeUSDTDialog(Context context, String usdtAddress /*充值的目标地址*/, String money) {
        super(context, R.style.MyDialog);
        this.mContext = context;
        /*this.name = name;
        this.card = card;
        this.bandName = bandName; */
        this.money = money;
        this.usdtAddress = usdtAddress;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rechrage_by_usdt);
        initView();
        initData();
        initEvent();

        // 屏蔽back键
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.9);
            window.setAttributes(lp);
            window.setGravity(Gravity.CENTER);
        }
    }

    private void initView() {
        mCloseIv = findViewById(R.id.close_iv);
        mTipTv = findViewById(R.id.tip_tv);
        mConfirmTv = findViewById(R.id.confirm);
        mCancelTv = findViewById(R.id.cancel);
        mTv1 = findViewById(R.id.tv1);
        mTv2 = findViewById(R.id.tv2);
        mSp2 = findViewById(R.id.dialog_recharge_sp2);
        mTv3 = findViewById(R.id.tv3);
        mIv1 = findViewById(R.id.iv1);
        mIv2 = findViewById(R.id.iv2);
        mIv3 = findViewById(R.id.iv3);
        etAdress = findViewById(R.id.tv1_content);
        paymentAdress = findViewById(R.id.second_et);
    }

    private void initData() {
//        mTipTv.setText(mContext.getString(R.string.scan_recharge_band_tip1
//                , CoreManager.requireSelf(MyApplication.getContext()).getAccount()));
//        mTv1.setText(mContext.getString(R.string.scan_recharge_band_body1, name));
//        mTv2.setText(mContext.getString(R.string.scan_recharge_band_body2, card));
//        mTv3.setText(mContext.getString(R.string.scan_recharge_band_body3, bandName));
        // 设置usdt type adapter并设置默认值
        usdtTypeAdapter = ArrayAdapter.createFromResource(mContext, R.array.usdt_addr_type, R.layout.sp_usdt_type_item);
        usdtTypeAdapter.setDropDownViewResource(R.layout.sp_usdt_type_item);
        mSp2.setAdapter(usdtTypeAdapter);
        mSp2.setSelection(0);
//        mSp2.getSelectedView().setEnabled(false);
        mSp2.setEnabled(false);

        // 填充usdt地址
        etAdress.setText(usdtAddress);

        // 设置Tip联动
        // 获取需要充值的usdt数量
        String  usdtAmountStr = dcepToUsdt(money).toString();
        mTipTv.setText(mContext.getString(R.string.usdt_recharge_tip1, usdtAddress, usdtAmountStr, money));
    }

    private Double dcepToUsdt(String decpMoneyStr) {
        try {
            Double usdtToDcepRate = CoreManager.getInstance(mContext).getConfig().usdtExchangeRate;
            BigDecimal dcepMoney = BigDecimal.valueOf(Double.valueOf(decpMoneyStr));
            BigDecimal usdtAmount = dcepMoney.divide(BigDecimal.valueOf(usdtToDcepRate), 2, BigDecimal.ROUND_HALF_UP);
            return usdtAmount.doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Double.valueOf(0);
    }

    private void initEvent() {
        mCloseIv.setOnClickListener(v -> dismiss());
        mIv1.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("Label", usdtAddress);
                clipboardManager.setPrimaryClip(clipData);
                ToastUtil.showToast(mContext, mContext.getString(R.string.tip_copied_to_clipboard));
            }
        });
        /*mIv2.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("Label", card);
                clipboardManager.setPrimaryClip(clipData);
                ToastUtil.showToast(mContext, mContext.getString(R.string.tip_copied_to_clipboard));
            }
        });*/
        /*mIv3.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("Label", bandName);
                clipboardManager.setPrimaryClip(clipData);
                ToastUtil.showToast(mContext, mContext.getString(R.string.tip_copied_to_clipboard));
            }
        });*/
        /*mTipTv.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("Label", CoreManager.requireSelf(MyApplication.getContext()).getAccount());
                clipboardManager.setPrimaryClip(clipData);
                ToastUtil.showToast(mContext, mContext.getString(R.string.label_communication) + mContext.getString(R.string.tip_copied_to_clipboard));
            }
        });*/
        mConfirmTv.setOnClickListener(v -> recharge());
        mCancelTv.setOnClickListener(v -> dismiss());
    }

    private void recharge() {
        String paymentAddress = paymentAdress.getText().toString();
        if (paymentAddress == null || paymentAddress.length() <1) {
            ToastUtil.showToast(mContext, mContext.getString(R.string.required_recharge_usdt_paymentaddress));
            return;
        }
        if (TextUtils.isEmpty(usdtAddress)) {
            ToastUtil.showToast(mContext, mContext.getString(R.string.required_recharge_usdt_platformaddress));
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(mContext);
        Map<String, String> params = new HashMap<>();
        params.put("address", paymentAddress);
        params.put("amount", money);

        HttpUtils.get().url(CoreManager.requireConfig(mContext).MANUAL_PAY_USDT_RECHARGE)
                .params(params)
                .build()
                .execute(new BaseCallback<ScanRecharge>(ScanRecharge.class) {

                    @Override
                    public void onResponse(ObjectResult<ScanRecharge> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(mContext, mContext.getString(R.string.send_recharge_usdt_request_status_ok));
                            RechargeUSDTDialog.this.dismiss();
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
