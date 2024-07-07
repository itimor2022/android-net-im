package com.sk.weichat.ui.me;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sk.weichat.AppConfig;
import com.sk.weichat.R;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.tool.ButtonColorChange;
import com.sk.weichat.ui.tool.WebViewActivity;

/**
 * @author Anlycal<远>
 * @date 2024/6/9
 * @description ...
 */


public class VipLevelActivity extends BaseActivity {
    private Button mBtnVip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_level);
        getSupportActionBar().hide();
        initView();
        initEvent();
    }

    private void initView() {
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.membership_level));
//        tvTitle.setText("会员等级");

        mBtnVip = (Button) findViewById(R.id.vip_btn);
        ButtonColorChange.colorChange(mContext, mBtnVip);
    }

    private void initEvent(){
        mBtnVip.setOnClickListener(v->{
            WebViewActivity.start(this, AppConfig.promotionUrl,true);
        });
    }
}