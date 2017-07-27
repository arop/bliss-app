package com.arop.bliss_app.apiObjects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by andre on 27/07/2017.
 */

public class Share {
    @SerializedName("status")
    private String status = "";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
