package com.arop.bliss_app.networkUtils;

/**
 * Created by andre on 26/07/2017.
 */

public class ConnectivityEvent {
    private boolean isConnected;

    public ConnectivityEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
