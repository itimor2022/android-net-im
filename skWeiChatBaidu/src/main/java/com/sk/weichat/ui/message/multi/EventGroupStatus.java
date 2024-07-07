package com.sk.weichat.ui.message.multi;

public class EventGroupStatus {
    private final int whichStatus;
    private final int groupManagerStatus;
    public  String extraData;

    public EventGroupStatus(int whichStatus, int groupManagerStatus) {
        this.whichStatus = whichStatus;
        this.groupManagerStatus = groupManagerStatus;
    }

    public int getWhichStatus() {
        return whichStatus;
    }

    public int getGroupManagerStatus() {
        return groupManagerStatus;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}
