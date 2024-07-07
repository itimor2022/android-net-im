package com.sk.weichat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.sk.weichat.R;

public class DialogCertificate extends Dialog implements View.OnClickListener{
    private TextView contentNameTex;
    private TextView contentNoTxt;
    private TextView titleTxt;
    private Button submitTxt;
    private Button cancelTxt;

    private Context mContext;
    private String contentName;
    private String contentNo;
    private OnCloseListener listener;
    private String positiveName;
    private String negativeName;
    private String title;


    public DialogCertificate(Context context, String contentName, String contentNo) {
        super(context, R.style.HintDialog);
        this.mContext = context;
        this.contentName = contentName;
        this.contentNo = contentNo;
    }


    public DialogCertificate setTitle(String title){
        this.title = title;
        return this;
    }

    public DialogCertificate setPositiveButton(String name){
        this.positiveName = name;
        return this;
    }

    public DialogCertificate setNegativeButton(String name){
        this.negativeName = name;
        return this;
    }

    public DialogCertificate setContentNameText(String contentName){
        this.contentName = contentName;
        return this;
    }

    public DialogCertificate setContentNoText(String contentNo){
        this.contentNo = contentNo;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_certificate);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
    }

    private void initView(){
        contentNameTex = findViewById(R.id.content_name);
        contentNoTxt = findViewById(R.id.content_card_no);

        titleTxt = findViewById(R.id.title);
        submitTxt = findViewById(R.id.submit);
        submitTxt.setOnClickListener(this);
        cancelTxt = findViewById(R.id.cancel);
        cancelTxt.setOnClickListener(this);

        if(!TextUtils.isEmpty(contentName)){
            contentNameTex.setText(contentName);
        }

        if(!TextUtils.isEmpty(contentNo)){
            contentNoTxt.setText(contentNo);
        }

        if(!TextUtils.isEmpty(positiveName)){
            submitTxt.setText(positiveName);
        }

        if(!TextUtils.isEmpty(positiveName)){
            submitTxt.setText(positiveName);
        }

        if(!TextUtils.isEmpty(negativeName)){
            cancelTxt.setText(negativeName);
        }

        if(!TextUtils.isEmpty(title)){
            titleTxt.setText(title);
            titleTxt.setVisibility(View.VISIBLE);
        }else{
            //titleTxt.setVisibility(View.GONE);
        }
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = mContext.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.5); // 高度设置为屏幕的0.6
        lp.height = (int) (d.heightPixels * 0.5);
        dialogWindow.setAttributes(lp);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel:
                if(listener != null){
                    listener.onClick(this, false);
                }
                this.dismiss();
                break;
            case R.id.submit:
                if(listener != null){
                    listener.onClick(this, true);
                }
                break;
        }
    }

    public interface OnCloseListener{
        void onClick(Dialog dialog, boolean confirm);
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        return super.onTouchEvent(event);
    }
}
