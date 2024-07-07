package com.sk.weichat.ui.me;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oneim.chat2022_pro.HostHelper;
import com.sk.weichat.AppConfig;
import com.sk.weichat.AppConstant;
import com.sk.weichat.BuildConfig;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.ConfigBean;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.ui.SplashActivity;
import com.sk.weichat.ui.base.BaseActivity;
import com.sk.weichat.ui.base.CoreManager;
import com.sk.weichat.ui.base.SetActionBarActivity;
import com.sk.weichat.util.PreferenceUtils;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class SelectConfigActivity extends SetActionBarActivity {

    private RecyclerView recyclerView;
    private MHandle handle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_config);
        initActionBar();

        initSelectAdapter();
        initData();
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
        tvTitle.setText("切换线路");
    }

    private void initSelectAdapter() {
        recyclerView = findViewById(R.id.recyclerView);
        List<SelectBean> list = new ArrayList<>();
        for (int i = 0; i < AppConfig.HOST_LIST.length;i++){
            list.add(new SelectBean(CoreManager.currLoginPipe == i,AppConfig.HOST_LIST[i],"线路" + i));
        }

        SelectAdapter adapter = new SelectAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn).setOnClickListener(v -> startActivity(new Intent(this,SetConfigActivity.class)));
    }

    private void initData(){
        handle = new MHandle(this);
        HostHelper.getInstance().excuteHost(AppConfig.IMG_URL, getCacheDir().getAbsolutePath(), new HostHelper.HostCallback() {
            @Override
            public void hostCallback(String hostList) {
                if (hostList == null){
                    return;
                }

                Message msg = handle.obtainMessage();
                msg.what = 1;
                msg.obj = hostList;
                handle.sendMessage(msg);
            }
        });
    }

    private void updateList(String json){

        List<String> urls = new ArrayList<>();
        try {
            JSONObject jo = new JSONObject(json);
            JSONArray ja = new JSONArray(jo.optString("data"));
            for (int i = 0; i < ja.length();i++){
                JSONObject j = ja.getJSONObject(i);
                urls.add(j.getString("zoneName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<SelectBean> list = ((SelectAdapter)recyclerView.getAdapter()).getData();
        int start = list.size();
        int end = list.size() + urls.size();
        for (int i = start;i < end;i++){
            ((SelectAdapter)recyclerView.getAdapter()).addData(new SelectBean(CoreManager.currLoginPipe == i,urls.get(i-start),"线路" + i));
        }
    }

    private void getConfig(int position) {
        String mConfigApi = AppConfig.readConfigUrl(this);

        Map<String, String> params = new HashMap<>();
        Reporter.putUserData("configUrl", mConfigApi);
        long requestTime = System.currentTimeMillis();
        HttpUtils.get().url(mConfigApi)
                .params(params)
                .build(true, true)
                .execute(new BaseCallback<ConfigBean>(ConfigBean.class) {
                    @Override
                    public void onResponse(ObjectResult<ConfigBean> result) {
                        if (result != null) {
                            long responseTime = System.currentTimeMillis();
                            TimeUtils.responseTime(requestTime, result.getCurrentTime(), result.getCurrentTime(), responseTime);
                        }
                        ConfigBean configBean;
                        if (result == null || result.getData() == null || result.getResultCode() != Result.CODE_SUCCESS) {
                            Log.e("zq", "获取网络配置失败，使用已经保存了的配置");

                            ToastUtil.showToast(SelectConfigActivity.this,"配置失败，请重新选择线路");
                            // 获取网络配置失败，使用已经保存了的配置，
                            configBean = CoreManager.getInstance(SelectConfigActivity.this).readConfigBean();
                        } else {
                            Log.e("zq", "获取网络配置成功，使用服务端返回的配置并更新本地配置");
                            configBean = result.getData();
                            if (!TextUtils.isEmpty(configBean.getAddress())) {
                                PreferenceUtils.putString(SelectConfigActivity.this, AppConstant.EXTRA_CLUSTER_AREA, configBean.getAddress());
                            }
                            CoreManager.getInstance(SelectConfigActivity.this).saveConfigBean(configBean);
                            MyApplication.IS_OPEN_CLUSTER = configBean.getIsOpenCluster() == 1 ? true : false;

                            ToastUtil.showToast(SelectConfigActivity.this,"切换成功");
                            ((SelectAdapter)recyclerView.getAdapter()).select(position);
                            CoreManager.currLoginPipe = position;
                            AppConfig.CONFIG_URL =  ((SelectAdapter)recyclerView.getAdapter()).getData().get(position).url;
                            AppConfig.saveConfigUrl(SelectConfigActivity.this,  ((SelectAdapter)recyclerView.getAdapter()).getData().get(position).url);
                        }
                        setConfig(configBean);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e("zq", "获取网络配置失败，使用已经保存了的配置");
                        // ToastUtil.showToast(SplashActivity.this, R.string.tip_get_config_failed);
                        // 获取网络配置失败，使用已经保存了的配置，
                        ToastUtil.showToast(SelectConfigActivity.this,"配置失败，请重新选择线路");
                        ConfigBean configBean = CoreManager.getInstance(SelectConfigActivity.this).readConfigBean();
                        setConfig(configBean);
                    }
                });
    }

    private void setConfig(ConfigBean configBean) {
        if (configBean == null) {
            if (BuildConfig.DEBUG) {
                ToastUtil.showToast(this, R.string.tip_get_config_failed);
            }

            // 如果没有保存配置，也就是第一次使用，就连不上服务器，使用默认配置
            configBean = CoreManager.getDefaultConfig(this);
            if (configBean == null) {
                // 不可到达，本地assets一定要提供默认config,
                DialogHelper.tip(this, getString(R.string.tip_get_config_failed));
                return;
            }
            CoreManager.getInstance(SelectConfigActivity.this).saveConfigBean(configBean);
        }

        MyApplication.IS_SUPPORT_SECURE_CHAT = configBean.getIsOpenSecureChat() == 1;
    }


    class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.SelectHolder> {

        private List<SelectBean> list;

        public SelectAdapter(List<SelectBean> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public SelectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SelectHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_select_config, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SelectHolder holder, int position) {
            holder.tv.setText(list.get(position).name);

            holder.iv.setImageResource(list.get(position).isSelect ? R.drawable.select_true : R.drawable.oval_false);

            final int finalPosition = position;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getConfig(finalPosition);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public List<SelectBean> getData(){
            return list;
        }

        public void addData(SelectBean selectBean){
            list.add(selectBean);
            notifyDataSetChanged();
        }

        public void select(int position){
            for (SelectBean bean:list){
                bean.isSelect = false;
            }

            list.get(position).isSelect = true;
            notifyDataSetChanged();
        }

        class SelectHolder extends RecyclerView.ViewHolder {

            TextView tv;
            ImageView iv;

            public SelectHolder(@NonNull View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.tv);
                iv = itemView.findViewById(R.id.iv);
            }
        }
    }

    class SelectBean {
        public boolean isSelect;

        public String url;

        public String name;

        public SelectBean() {
        }

        public SelectBean(boolean isSelect, String url, String name) {
            this.isSelect = isSelect;
            this.url = url;
            this.name = name;
        }
    }

    static class MHandle extends Handler{

        private WeakReference<SelectConfigActivity> wrCtx;

        MHandle(SelectConfigActivity configActivity){
            wrCtx = new WeakReference<>(configActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            wrCtx.get().updateList((String) msg.obj);
        }
    }
}