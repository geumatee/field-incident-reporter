package com.geumatee.fieldincidentreporter

import android.app.Application
import com.geumatee.fieldincidentreporter.data.IncidentApiService
import com.geumatee.fieldincidentreporter.data.IncidentRepository
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class IncidentApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}

class AppContainer {
    // 10.0.2.2 Android emulator host machine's loopback interface
    private val baseUrl = "http://10.0.2.2:3001/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val apiService: IncidentApiService by lazy {
        val gson = GsonBuilder().serializeNulls().create()
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(IncidentApiService::class.java)
    }

    val incidentRepository: IncidentRepository by lazy {
        IncidentRepository(apiService)
    }
}