package com.sk.weichat.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.sk.weichat.R;
import com.sk.weichat.adapter.base.CommonRecyclerAdapter;
import com.sk.weichat.adapter.base.ViewHolder;
import com.xuan.xuanhttplibrary.okhttp.result.response.VipInfo;

import java.util.List;

public class VipCardAdapter extends CommonRecyclerAdapter<VipInfo> {
    public VipCardAdapter(Context context, List<VipInfo> datas) {
        super(context, datas, R.layout.item_vip_card);
    }

    @Override
    public void convert(RecyclerView.ViewHolder holder, VipInfo item, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setText(R.id.tv_vip_code,item.getNumber());
        viewHolder.setText(R.id.tv_vip_serial,"序列号 "+item.getSerial());
        viewHolder.setText(R.id.tv_address,item.getAddress());
    }
}
