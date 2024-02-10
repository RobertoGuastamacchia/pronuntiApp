package it.uniba.dib.sms232421.utility;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import it.uniba.dib.sms232421.notifications.MyFirebaseMessagingService;

public class PronuntiApp extends Application {

    private static boolean isAppJustStarted = true;
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new MyFirebaseMessagingService.AppLifecycleHandler());
    }

    public static boolean isAppJustStarted() {
        return isAppJustStarted;
    }

    public static void setAppJustStarted(boolean appJustStarted) {
        isAppJustStarted = appJustStarted;
    }
}
