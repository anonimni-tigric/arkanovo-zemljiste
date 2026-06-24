package com.example.kolokvijum2;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("continents")
    Call<List<Continent>> getContinents();
}