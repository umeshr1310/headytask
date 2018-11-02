package com.headytask.headytask.api;

import com.headytask.headytask.model.MainJson;


import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {
    public static String url="https://stark-spire-93433.herokuapp.com/";

    @GET("json")
    Call<MainJson> getApiData();
}
