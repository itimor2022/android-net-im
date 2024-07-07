package com.sk.weichat.bean.redpacket;

/**
 * Created by Administrator on 2016/9/18.
 */
public class Balance {
    private String appId;
    private String nonceStr;
    private String partnerId;
    private int withdrawStyleSwitch;
    private String withdrawRate; // 提现费率
    private Double usdtExchangeRate; // usdt汇率
    private int payStyleSwitch; // 充值方式开关 0-手动扫码充值 1-自动充值


    @Override
    public String toString() {
        return "Balance{" +
                "appId='" + appId + '\'' +
                ", nonceStr='" + nonceStr + '\'' +
                ", partnerId='" + partnerId + '\'' +
                ", prepayId='" + prepayId + '\'' +
                ", sign='" + sign + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", Balance=" + Balance +
                '}';
    }

    public String prepayId;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPrepayId() {
        return prepayId;
    }

    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String sign;
    public String timeStamp;

    public double getBalance() {
        return Balance;
    }

    public void setBalance(double balance) {
        Balance = balance;
    }

    private double Balance;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getWithdrawStyleSwitch() {
        return withdrawStyleSwitch;
    }

    public void setWithdrawStyleSwitch(int withdrawStyleSwitch) {
        this.withdrawStyleSwitch = withdrawStyleSwitch;
    }

    public String getWithdrawRate() {
        return withdrawRate;
    }

    public void setWithdrawRate(String withdrawRate) {
        this.withdrawRate = withdrawRate;
    }

    public Double getUsdtExchangeRate() {
        return usdtExchangeRate;
    }

    public void setUsdtExchangeRate(Double usdtExchangeRate) {
        this.usdtExchangeRate = usdtExchangeRate;
    }

    public int getPayStyleSwitch() {
        return payStyleSwitch;
    }

    public void setPayStyleSwitch(int payStyleSwitch) {
        this.payStyleSwitch = payStyleSwitch;
    }
}
