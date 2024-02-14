package it.uniba.dib.sms2324FF2.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils extends BroadcastReceiver {

    private NetworkChangeListener listener;

    // Metodo per verificare la presenza di internet sul device
    public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    public interface NetworkChangeListener {
        void onNetworkChange(boolean isConnected);
    }

    public void setNetworkChangeListener(NetworkChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener != null) {
            boolean isConnected = isConnected(context);
            listener.onNetworkChange(isConnected);
        }
    }
}

