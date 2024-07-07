package com.xuan.xuanhttplibrary.okhttp.result.response;

import java.io.Serializable;
import java.util.List;

/**
 * @Author Roken
 * @Time 2022/8/19
 * @Describe 描述
 */
public class VipHas implements Serializable {


    /**
     * certUrl :
     * flag : false
     */

    private String certUrl;
    private boolean flag;
    private boolean identityState;
    private List<String> adminList;
    //    "serial": "DFDB4745FEC5R000000054372",
//            "number": "6666666666654372",
//            "phonics": "BJGG",
//            "address": "北京故宫"
    private String serial;
    private String number;
    private String phonics;
    private String address;

    public String getSerial() {
        return serial;
    }

    public String getNumber() {
        return number;
    }

    public String getAddress() {
        return address;
    }

    public String getPhonics() {
        return phonics;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhonics(String phonics) {
        this.phonics = phonics;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCertUrl() {
        return certUrl;
    }

    public void setCertUrl(String certUrl) {
        this.certUrl = certUrl;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean isIdentityState() {
        return identityState;
    }

    public void setIdentityState(boolean identityState) {
        this.identityState = identityState;
    }

    public List<String> getAdminList() {
        return adminList;
    }

    public void setAdminList(List<String> adminList) {
        this.adminList = adminList;
    }
}
