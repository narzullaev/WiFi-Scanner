package com.example.sardor.wifiscanner;

public class ScanInfo {

    private String bssid;
    private String ssid;
    private String level;
    private String frequency;
    private String time;
    private double latitude;
    private double longitude;
    private double accuracy;

    public ScanInfo(String bssid, String ssid, String level, String frequency,
                    String time, double latitude, double longitude, double accuracy) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.level = level;
        this.frequency = frequency;
        this.time = time;
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
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
