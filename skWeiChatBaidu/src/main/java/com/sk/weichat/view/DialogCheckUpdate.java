package com.sk.weichat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.sk.weichat.R;

/**
 * @describe 版本更新提示框1
 * Created by Roken on 2018/5/15 0015.
 */

public class DialogCheckUpdate extends Dialog {


    private Context context;

    private TextView no,yes;

    private TextView tvRemark;

    private String remark;

    private ClickListenerInterface clickListenerInterface;

    private boolean isForce = false;

    public interface ClickListenerInterface {

        void clickYes();
        void clickNo();
    }

    public DialogCheckUpdate(Context context, String remark, boolean isForce) {
        super(context, R.style.OtherDialog);
        this.context = context;
        this.remark = remark;
        this.isForce = isForce;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_check_update, null);

        setContentView(view);

        no = view.findViewById(R.id.tv_no);
        yes = view.findViewById(R.id.tv_yes);

        if(isForce){
            no.setVisibility(View.GONE);
        }else{
            no.setVisibility(View.VISIBLE);
        }

        tvRemark = view.findViewById(R.id.tv_remark);
        tvRemark.setText(this.remark);
        tvRemark.setMovementMethod(ScrollingMovementMethod.getInstance());

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        lp.height = (int) (d.heightPixels * 0.9);

        no.setOnClickListener(new ClickListener());
        yes.setOnClickListener(new ClickListener());
        dialogWindow.setAttributes(lp);
        setCancelable(false);
    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.tv_yes:
                    clickListenerInterface.clickYes();
                    break;
                case R.id.tv_no:
                    clickListenerInterface.clickNo();
                    break;
            }
        }

    }

}
