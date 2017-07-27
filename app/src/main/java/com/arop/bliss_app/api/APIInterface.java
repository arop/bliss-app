package com.arop.bliss_app.api;

import com.arop.bliss_app.apiObjects.Health;
import com.arop.bliss_app.apiObjects.Question;
import com.arop.bliss_app.apiObjects.Share;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by andre on 25/07/2017.
 */

public interface APIInterface {
    /**
     * Check health
     * Returns: "OK", "NOT OK"
     */
    @GET("/health")
    Call<Health> getHealth();

    /**
     * Get all questions
     * URL: base + /questions?limit&offset&filter
     */
    @GET("/questions")
    Call<ArrayList<Question>> getQuestions(@Query("limit") int limit, @Query("offset") int offset, @Query("filter") String filter);

    /**
     * Get single question
     * @param id question id
     */
    @GET("/questions/{id}")
    Call<Question> getQuestion(@Path("id") int id);

    /**
     * Vote on a question
     * @param id question id
     */
    @PUT("/questions/{id}")
    Call<Question> vote(@Path("id") int id, @Body Question question);

    @POST("/share")
    Call<Share> share(@Query("destination_email") String destination_email, @Query("content_url") String content_url);
}
