package com.sschoi.notifyninja.model;

public class AppModel {
    private final String packageName;
    private final String appName;
    private final String phone; // 받을 번호
    private final String senderNameFilter; // 보낸사람 이름 필터(옵션)
    private final String senderNumberFilter; // 보낸사람 번호 필터(옵션)

    public AppModel(String packageName, String appName, String phone) {
        this.packageName = packageName;
        this.appName = appName;
        this.phone = phone;
        this.senderNameFilter = null;
        this.senderNumberFilter = null;
    }

    public AppModel(String packageName, String appName, String phone, String senderNameFilter, String senderNumberFilter) {
        this.packageName = packageName;
        this.appName = appName;
        this.phone = phone;
        this.senderNameFilter = senderNameFilter;
        this.senderNumberFilter = senderNumberFilter;
    }

    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public String getPhone() { return phone; }
    public String getSenderNameFilter() { return senderNameFilter; }
    public String getSenderNumberFilter() { return senderNumberFilter; }
}
