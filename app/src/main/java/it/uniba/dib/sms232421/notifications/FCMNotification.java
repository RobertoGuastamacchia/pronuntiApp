package it.uniba.dib.sms232421.notifications;

import com.google.gson.annotations.SerializedName;

public class FCMNotification {
    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    public FCMNotification(String title, String body){
        this.body = body;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
