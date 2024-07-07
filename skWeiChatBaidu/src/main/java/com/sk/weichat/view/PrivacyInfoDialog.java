package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.sk.weichat.R;
import com.sk.weichat.ui.me.AboutActivity;
import com.sk.weichat.ui.other.PrivacyAgreeActivity;
import com.sk.weichat.util.UiUtils;

/**
 * @Author Roken
 * @Time 2022/6/10
 * @Describe 描述
 */
public class PrivacyInfoDialog extends Dialog implements View.OnClickListener {

    private TextView tvInfo;

    private Context mContext;
    private OnClickListener onClickListener;


    public interface OnClickListener {
        //同意
        void onAgree();
        // 退出
        void onCancel();
        void onPrivacy1();
        void onPrivacy2();
    }

    public PrivacyInfoDialog(@NonNull Context context, OnClickListener onClickListener) {
        super(context, R.style.BottomDialog);
        this.mContext = context;
        this.onClickListener = onClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_privacy_info);

        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        tvInfo = findViewById(R.id.tv_info);
        initView();
    }

    private void initView() {
        //用户协议
        try {
            String strInfo = mContext.getString(R.string.privacy_dialog_info);
            String strAgreen1 = mContext.getString(R.string.login_agreement_2);
            String strAgreen2 = mContext.getString(R.string.login_agreement_3);
            SpannableString ts = new SpannableString(strInfo);

            int index1 = strInfo.indexOf(strAgreen1);
            int index2 = strInfo.indexOf(strAgreen2);
            //设置点击事件
            ts.setSpan(new Clickable(registerClickListener), index1, index1 + strAgreen1.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ts.setSpan(new Clickable(privacyListener), index2, index2 + strAgreen2.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvInfo.setText(ts);
            tvInfo.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (Exception e) {
        }

    }

    private View.OnClickListener registerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(onClickListener != null){
                onClickListener.onPrivacy1();
            }
        }
    };
    //用户协议
    private View.OnClickListener privacyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(onClickListener != null){
                onClickListener.onPrivacy2();
            }
        }
    };

    class Clickable extends ClickableSpan {
        private final View.OnClickListener mListener;

        public Clickable(View.OnClickListener l) {
            mListener = l;
        }

        /**
         * 重写父类点击事件
         */
        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
        /**
         * 重写父类updateDrawState方法 我们可以给TextView设置字体颜色,背景颜色等等...
         */
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ContextCompat.getColor(mContext, R.color.color_role3));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_ok:
                if(onClickListener != null){
                    onClickListener.onAgree();
                }
                break;
            case R.id.btn_cancel:
                if(onClickListener != null){
                    onClickListener.onCancel();
                }
                break;
        }
    }

}