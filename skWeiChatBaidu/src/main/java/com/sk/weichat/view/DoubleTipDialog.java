package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sk.weichat.R;
import com.sk.weichat.util.ScreenUtil;

/**
 * @Author Roken
 * @Time 2022/6/10
 * @Describe 描述
 */
public class DoubleTipDialog extends Dialog {

    private TextView tvContentMessage, mConfirm, mCancel;
    private String message;
    private TipDialog.ConfirmOnClickListener mConfirmOnClickListener;

    public DoubleTipDialog(Context context, String message) {
        super(context, R.style.BottomDialog);
        this.message = message;
    }


    public void setConfirmOnClickListener(TipDialog.ConfirmOnClickListener mConfirmOnClickListener) {
        this.mConfirmOnClickListener = mConfirmOnClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_double_tip);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        tvContentMessage = findViewById(R.id.tv_content_message);
        mConfirm = findViewById(R.id.confirm);
        mCancel = findViewById(R.id.cancel);
        if(!TextUtils.isEmpty(message)){
            tvContentMessage.setText(message);
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.9);
        initEvent();
    }

    private void initEvent() {
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mConfirmOnClickListener != null) {
                    mConfirmOnClickListener.confirm();
                }
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        countDown();
    }

    public interface ConfirmOnClickListener {
        void confirm();
    }

    private CountDownTimer countDownTimer;
    private String mConfirmText;

    private void countDown(){
        mConfirmText = mConfirm.getText().toString();
        countDownTimer = new CountDownTimer(15 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mConfirm.setEnabled(false);
                mConfirm.setText(mConfirmText+"("+ millisUntilFinished / 1000 +")");
            }

            @Override
            public void onFinish() {
                mConfirm.setEnabled(true);
                mConfirm.setText(mConfirmText);
            }
        };
        countDownTimer.start();
    }

    @Override
    public void setOnDismissListener(@Nullable DialogInterface.OnDismissListener listener) {
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
        super.setOnDismissListener(listener);
    }
}
