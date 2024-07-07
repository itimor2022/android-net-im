package com.xuan.xuanhttplibrary.okhttp;

import android.util.Log;

import com.sk.weichat.util.Constants;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @Author Roken
 * @Time 2022/5/6
 * @Describe server
 */
public class Api {

    private static volatile Api instance;

    private Retrofit retrofit;
    private AppRetrofit apiService;  //app服务


    public static Api getInstance() {
        if (instance == null) {
            synchronized (Api.class) {
                if (instance == null) {
                    instance = new Api();
                }
            }
        }
        return instance;
    }

    /**
     * APP模块
     * 服务名：app-server
     *
     * @return
     */
    public AppRetrofit getAppRetrofit() {
        getRetrofitService();
        //创建—— 网络请求接口—— 实例
        if (apiService == null) {
            apiService = retrofit.create(AppRetrofit.class);
        }
        return apiService;
    }



    private void getRetrofitService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    //设置网络请求的Url地址
                    .baseUrl(Constants.APP_URL)
                    //设置数据解析器
                    .addConverterFactory(GsonConverterFactory.create())
                    //设置网络请求适配器，使其支持RxJava与RxAndroid
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(createOkHttpClient())
                    .build();
        }
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        return createResponse(chain);
//                    }
//                })
                .addInterceptor(new LoggingInterceptor())
//                .addInterceptor(httpLoggingInterceptor)
                .build();
        return httpClient;
    }

    /*create Response*/
    private Response createResponse(Interceptor.Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();
        return chain.proceed(requestBuilder.build());
    }

    public HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(String message) {
            Log.e("info", message);
        }
    });
}
