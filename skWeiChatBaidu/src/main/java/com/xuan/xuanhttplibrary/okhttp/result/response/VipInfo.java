package com.xuan.xuanhttplibrary.okhttp.result.response;

import java.io.Serializable;

/**
 * @Author Roken
 * @Time 2022/8/19
 * @Describe 描述
 */
public class VipInfo implements Serializable {


    /**
     * serial : DFDB4745FEC5R000000000001
     * number : 6666666666600001
     * phonics : BJGG
     */

    private String serial;
    private String number;
    private String phonics;
    private String address;

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhonics() {
        return phonics;
    }

    public void setPhonics(String phonics) {
        this.phonics = phonics;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
