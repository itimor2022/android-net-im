package com.xuan.xuanhttplibrary.okhttp.result;

/**
 * @Author Roken
 * @Time 2022/8/19
 * @Describe 描述
 */
public class BaseResult<T> {

    private T data;

    private String code;

    private String msg;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
