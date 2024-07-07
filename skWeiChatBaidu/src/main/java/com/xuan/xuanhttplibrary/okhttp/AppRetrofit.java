package com.xuan.xuanhttplibrary.okhttp;

import com.xuan.xuanhttplibrary.okhttp.result.BaseResult;
import com.xuan.xuanhttplibrary.okhttp.result.response.ActiveState;
import com.xuan.xuanhttplibrary.okhttp.result.response.VipHas;
import com.xuan.xuanhttplibrary.okhttp.result.response.VipInfo;
import com.xuan.xuanhttplibrary.okhttp.result.response.VipInfos;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * APP模块
 * @Author Roken
 * @Time 2022/5/6
 * @Describe app server接口
 */
public interface AppRetrofit {


    /**
     * 激活VIP
     * @param
     * @return
     */
    @Headers({"Content-Type: application/json"})
    @POST("/api/user/cert/activate")
    Observable<BaseResult> activateVip(@Body Map map);

    @Headers({"Content-Type: application/json"})
    @POST("/api/user/cert/userOptionCertActivate")
    Observable<BaseResult> activateCert(@Body Map map);

    /**
     * 获取VIP
     * @param
     * @return
     */
    @Headers({"Content-Type: application/json"})
    @POST("/api/user/cert/getVipInfo")
    Observable<BaseResult<VipInfos>> getVipInfo(@Body Map map);

    /**
     * 认证
     * @param
     * @return
     */
    @Headers({"Content-Type: application/json"})
    @POST("/api/user/cert/identityIdCard")
    Observable<BaseResult> identityIdCard(@Body Map map);

    @Headers({"Content-Type: application/json"})
    @POST("/api/user/cert/getActivateBycatetory")
    Observable<BaseResult<ActiveState>> getActivateBycatetory(@Body Map map);

}
