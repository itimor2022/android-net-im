package com.sk.weichat.adapter.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 通用的Adapter
 */
public abstract class CommonRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Context mContext;
    protected LayoutInflater mInflater;
    protected List<T> mDatas;
    private int mLayoutId;
    // 多布局支持
    private MultiTypeSupport mMultiTypeSupport;
    private OnItemClickListener mOnItemClickListener;//声明接口
    private FrameLayout mEmptyLayout;

    public CommonRecyclerAdapter(Context context, List<T> datas, int layoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = datas;
        this.mLayoutId = layoutId;
    }

    public List<T> getData() {
        return mDatas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 先inflate数据
        View itemView = mInflater.inflate(mLayoutId, parent, false);
        // 返回ViewHolder
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        convert(holder, mDatas.get(position), position);
        View itemView = holder.itemView;
        if (mOnItemClickListener != null) {
            itemView.setOnClickListener(v -> {
                int position1 = holder.getLayoutPosition();
                mOnItemClickListener.onItemClick(holder.itemView, position1);
            });
        }
    }

    public void addData(T data) {
        if(mDatas != null) {
            mDatas.add(data);
            notifyDataSetChanged();
        }
    }

    /**
     * 将数据添加到尾部
     */
    public void addAll(List<T> data) {
        mDatas.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * 替换刷新数据源
     */
    public void updateListView(List<T> data) {
        mDatas.clear();
        addAll(data);
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    /**
     * 利用抽象方法回传出去，每个不一样的Adapter去设置
     *
     * @param item 当前的数据
     */
    public abstract void convert(RecyclerView.ViewHolder holder, T item, int position);

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    /**
     * 多布局支持
     */
    public CommonRecyclerAdapter(Context context, List<T> data, MultiTypeSupport<T> multiTypeSupport) {
        this(context, data, -1);
        this.mMultiTypeSupport = multiTypeSupport;
    }

    /**
     * 根据当前位置获取不同的viewType
     */
    @Override
    public int getItemViewType(int position) {
        // 多布局支持
        if (mMultiTypeSupport != null) {
            return mMultiTypeSupport.getLayoutId(mDatas.get(position), position);
        }
        return super.getItemViewType(position);
    }


}

