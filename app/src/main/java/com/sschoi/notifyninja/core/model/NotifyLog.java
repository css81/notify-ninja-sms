package com.sschoi.notifyninja.core.model;

public class ForwardLog {
    private int id;           // DB PK
    private String time;        // 발송 시간 (timestamp)
    private String pkg;       // 알림 보낸 앱 패키지명
    private String target;    // 수신 번호
    private String title;     // 알림 제목/내용
    private String status;    // SENT / FAILED

    public ForwardLog() { }

    // getter / setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getPkg() { return pkg; }
    public void setPkg(String pkg) { this.pkg = pkg; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
