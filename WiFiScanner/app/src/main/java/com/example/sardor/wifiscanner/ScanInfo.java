package com.example.sardor.wifiscanner;

public class ScanInfo {

    private String bssid;
    private String ssid;
    private String level;
    private String frequency;
    private String time;

    public ScanInfo(String bssid, String ssid, String level, String frequency, String time) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.level = level;
        this.frequency = frequency;
        this.time = time;
    }

    public String getBssid() {
        return bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public String getLevel() {
        return level;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getTime() {
        return time;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
