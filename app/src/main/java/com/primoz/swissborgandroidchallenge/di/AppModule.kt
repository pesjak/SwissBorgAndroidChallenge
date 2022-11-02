package com.primoz.swissborgandroidchallenge.di

import com.primoz.swissborgandroidchallenge.BuildConfig
import com.primoz.swissborgandroidchallenge.helpers.Constants
import com.primoz.swissborgandroidchallenge.network.BitFinexAPI
import com.primoz.swissborgandroidchallenge.network.BitFinexClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    fun provideBaseUrl() = Constants.Rest.BASE_URL

    @Provides
    fun provideOkHttpClient() = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.Rest.TIMEOUT_DEFAULT, TimeUnit.SECONDS)
            .readTimeout(Constants.Rest.TIMEOUT_DEFAULT, TimeUnit.SECONDS)
            .writeTimeout(Constants.Rest.TIMEOUT_DEFAULT, TimeUnit.SECONDS)
            .build()
    } else {
        OkHttpClient
            .Builder()
            .build()
    }

    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ) = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(provideBaseUrl())
        .client(okHttpClient)
        .build()

    @Provides
    fun provideApi(retrofit: Retrofit) = retrofit.create(BitFinexAPI::class.java)

    @Provides
    fun provideSwissBorgClient(api: BitFinexAPI): BitFinexClient = BitFinexClient(api)

}
