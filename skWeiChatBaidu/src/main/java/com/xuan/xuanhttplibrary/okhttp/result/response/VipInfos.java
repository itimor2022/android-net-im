package com.xuan.xuanhttplibrary.okhttp.result.response;

import java.util.List;

public class VipInfos {

    private boolean activateViewFlag;
    private List<VipInfo> items;

    public List<VipInfo> getItems() {
        return items;
    }

    public boolean isActivateViewFlag() {
        return activateViewFlag;
    }

    public void setItems(List<VipInfo> items) {
        this.items = items;
    }

    public void setActivateViewFlag(boolean activateViewFlag) {
        this.activateViewFlag = activateViewFlag;
    }
}
