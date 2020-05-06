package com.elyeproj.wikisearchcount

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query

interface WikiApiService {

    @GET("api.php")
    fun hitCountCheckCall(@Query("action") action: String,
                        @Query("format") format: String,
                        @Query("list") list: String,
                        @Query("srsearch") srsearch: String): Call<Model.Result>

    @GET("api.php")
    fun hitCountCheckRx(@Query("action") action: String,
                        @Query("format") format: String,
                        @Query("list") list: String,
                        @Query("srsearch") srsearch: String): Single<Model.Result>

    @GET("api.php")
    fun hitCountCheckAsync(@Query("action") action: String,
                           @Query("format") format: String,
                           @Query("list") list: String,
                           @Query("srsearch") srsearch: String): Deferred<Model.Result>

    @GET("api.php")
    suspend fun hitCountCheckSuspend(@Query("action") action: String,
                           @Query("format") format: String,
                           @Query("list") list: String,
                           @Query("srsearch") srsearch: String): Model.Result

    companion object {
        fun create(): WikiApiService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://en.wikipedia.org/w/")
                    .build()

            return retrofit.create(WikiApiService::class.java)
        }
    }
}
