package it.uniba.dib.sms232421.notifications;

import com.google.gson.annotations.SerializedName;

public class FCMRequest {
    @SerializedName("to")
    private String to;  // Il token del dispositivo

    @SerializedName("notification")
    private FCMNotification notification;
    @SerializedName("priority")
    private String priority;

    public FCMRequest(String to, FCMNotification notification){
        this.to = to;
        this.notification = notification;
        this.priority = "high";
    }
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public FCMNotification getNotification() {
        return notification;
    }

    public void setNotification(FCMNotification notification) {
        this.notification = notification;
    }

}
