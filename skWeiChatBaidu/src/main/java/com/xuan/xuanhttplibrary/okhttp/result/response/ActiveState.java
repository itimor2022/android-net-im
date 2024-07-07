package com.xuan.xuanhttplibrary.okhttp.result.response;

public class ActiveState {

    private String certUrl;
    private boolean flag;

    public String getCertUrl() {
        return certUrl;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setCertUrl(String certUrl) {
        this.certUrl = certUrl;
    }
}
