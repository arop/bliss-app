package com.arop.bliss_app.networkUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.arop.bliss_app.ShowQuestionDetailsActivity;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by andre on 26/07/2017.
 */

public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";

    private EventBus bus = EventBus.getDefault();

    // post event if there is no Internet connection
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityEvent event = null;

        if (intent.getExtras() != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
            if(ni != null && ni.isConnectedOrConnecting()) {
                Log.i(TAG, "Network " + ni.getTypeName() + " connected");
                event = new ConnectivityEvent(true);
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d(TAG, "There's no network connectivity");
                event = new ConnectivityEvent(false);
            }
            // Post the event
            bus.post(event);
        }
    }
}
