package com.example.sardor.wifiscanner;

public class Temp {

    private String bssid;
    private String ssid;
    private String level;
    private String frequency;

    public Temp(String bssid, String ssid, String level, String frequency) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.level = level;
        this.frequency = frequency;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}
