package com.creditbank.mvp.dto;

public class PointOverviewDTO {
    private String date;
    private Integer earn;
    private Integer spend;
    private Integer convert;
    private Integer activity;
    private Integer net;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getEarn() {
        return earn;
    }

    public void setEarn(Integer earn) {
        this.earn = earn;
    }

    public Integer getSpend() {
        return spend;
    }

    public void setSpend(Integer spend) {
        this.spend = spend;
    }

    public Integer getConvert() {
        return convert;
    }

    public void setConvert(Integer convert) {
        this.convert = convert;
    }

    public Integer getActivity() {
        return activity;
    }

    public void setActivity(Integer activity) {
        this.activity = activity;
    }

    public Integer getNet() {
        return net;
    }

    public void setNet(Integer net) {
        this.net = net;
    }
}
