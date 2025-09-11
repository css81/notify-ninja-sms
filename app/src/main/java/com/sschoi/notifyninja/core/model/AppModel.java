package com.sschoi.notifyninja.model;

public class AppModel {
    private final String packageName;
    private final String appName;
    private final String phone; // 받을 번호

    public AppModel(String packageName, String appName, String phone) {
        this.packageName = packageName;
        this.appName = appName;
        this.phone = phone;
    }

    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public String getPhone() { return phone; }
}
