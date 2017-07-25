package com.arop.bliss_app.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
/**
 * Created by andre on 25/07/2017.
 */

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://private-bbbe9-blissrecruitmentapi.apiary-mock.com";

    public static Retrofit getClient() {
        if(retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
