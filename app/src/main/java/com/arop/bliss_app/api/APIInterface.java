package com.arop.bliss_app.api;

import com.arop.bliss_app.apiObjects.Health;
import com.arop.bliss_app.apiObjects.Question;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

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
    @GET("/questions?{limit}&{offset}&{filter}")
    Call<List<Question>> getQuestions(@Path("limit") int limit, @Path("offset") int offset, @Path("filter") String filter);

    @GET("/questions?{id}")
    Call<Question> getQuestion(@Path("id") int id);

}
