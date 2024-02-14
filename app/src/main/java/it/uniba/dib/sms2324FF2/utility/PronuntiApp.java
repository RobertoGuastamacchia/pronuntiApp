package it.uniba.dib.sms2324FF2.utility;

import android.app.Application;

import it.uniba.dib.sms2324FF2.notifications.MyFirebaseMessagingService;

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
