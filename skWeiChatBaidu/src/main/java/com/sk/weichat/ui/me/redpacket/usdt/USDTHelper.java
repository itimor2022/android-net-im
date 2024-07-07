package com.sk.weichat.ui.me.redpacket.usdt;

import android.content.Context;

import com.sk.weichat.R;
import com.sk.weichat.bean.WXUploadResult;
import com.sk.weichat.bean.event.EventNotifyByTag;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.helper.PaySecureHelper;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.dialog.money.WithdrawByUSDTDialog;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

public class USDTHelper {
    /**
     * 通知后台提现，
     */
    public static void withdraw(Context context, CoreManager coreManager, String money, String address, String password, WithdrawByUSDTDialog withdrawByUSDTDialog) {
        DialogHelper.showDefaulteMessageProgressDialog(context);

        final Map<String, String> params = new HashMap<>();
        params.put("amount", money);
        params.put("address", address);

        PaySecureHelper.generateParam(
                context, password, params,
                "" + money,
                t -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(context, context.getString(R.string.tip_pay_secure_place_holder, t.getMessage()));
                }, (p, code) -> {
                    HttpUtils.get().url(coreManager.getConfig().USDT_TRANSFER)
                            .params(p)
                            .build()
                            .execute(new BaseCallback<WXUploadResult>(WXUploadResult.class) {

                                @Override
                                public void onResponse(ObjectResult<WXUploadResult> result) {
                                    DialogHelper.dismissProgressDialog();
                                    if (Result.checkSuccess(context, result)) {
                                        withdrawByUSDTDialog.onUsdtWithDrawOk();
                                    }else {
                                        withdrawByUSDTDialog.onUsdtWithDrawError();
                                    }
                                }

                                @Override
                                public void onError(Call call, Exception e) {
                                    DialogHelper.dismissProgressDialog();
                                    ToastUtil.showErrorData(context);
                                }
                            });
                });
    }
}
