package com.github.billman64.nycschoolssatscores.Model

import android.telecom.Call
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ScoresAPI {

    @Headers(value = ["X-App-Token:  "])
    @GET("f9bf-2cp4.json?\$select=dbn,num_of_sat_test_takers,sat_critical_reading_avg_score,sat_math_avg_score,sat_writing_avg_score")
    fun getScores(@Query("dbn") dbn:String): retrofit2.Call<JsonArray>
}
