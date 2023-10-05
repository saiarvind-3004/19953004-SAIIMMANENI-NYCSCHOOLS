package com.github.billman64.nycschoolssatscores.Model

import android.telecom.Call
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Headers

interface SchoolsAPI {
    @Headers(value = ["X-App-Token:  "])
    @GET("s3k6-pzi2.json?\$select=dbn,school_name")
    fun getSchools(): retrofit2.Call<JsonArray>
}
