package com.elyeproj.wikisearchcount

import com.elyeproj.wikisearchcount.UrlConstant.BASE_PATH
import com.elyeproj.wikisearchcount.UrlConstant.BASE_URL
import com.elyeproj.wikisearchcount.UrlConstant.PARAM_ACTION
import com.elyeproj.wikisearchcount.UrlConstant.PARAM_FORMAT
import com.elyeproj.wikisearchcount.UrlConstant.PARAM_LIST
import com.elyeproj.wikisearchcount.UrlConstant.PARAM_SRSEARCH
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

    @GET(BASE_PATH)
    fun hitCountCheckCall(@Query(PARAM_ACTION) action: String,
                        @Query(PARAM_FORMAT) format: String,
                        @Query(PARAM_LIST) list: String,
                        @Query(PARAM_SRSEARCH) srsearch: String): Call<Model.Result>

    @GET(BASE_PATH)
    fun hitCountCheckRx(@Query(PARAM_ACTION) action: String,
                        @Query(PARAM_FORMAT) format: String,
                        @Query(PARAM_LIST) list: String,
                        @Query(PARAM_SRSEARCH) srsearch: String): Single<Model.Result>

    @GET(BASE_PATH)
    fun hitCountCheckAsync(@Query(PARAM_ACTION) action: String,
                           @Query(PARAM_FORMAT) format: String,
                           @Query(PARAM_LIST) list: String,
                           @Query(PARAM_SRSEARCH) srsearch: String): Deferred<Model.Result>

    @GET(BASE_PATH)
    suspend fun hitCountCheckSuspend(@Query(PARAM_ACTION) action: String,
                           @Query(PARAM_FORMAT) format: String,
                           @Query(PARAM_LIST) list: String,
                           @Query(PARAM_SRSEARCH) srsearch: String): Model.Result

    companion object {
        fun create(): WikiApiService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()

            return retrofit.create(WikiApiService::class.java)
        }
    }
}
