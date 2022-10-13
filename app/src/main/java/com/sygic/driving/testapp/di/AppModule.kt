package com.sygic.driving.testapp.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sygic.driving.testapp.core.auth.SygicAuth
import com.sygic.driving.testapp.core.driving.DrivingManager
import com.sygic.driving.testapp.core.platform.notification.NotificationProvider
import com.sygic.driving.testapp.core.settings.AppSettings
import com.sygic.driving.testapp.core.settings.AppSettingsImpl
import com.sygic.driving.testapp.core.utils.Constants
import com.sygic.driving.testapp.core.utils.Constants.DRB_SERVER_DATA_URL
import com.sygic.driving.testapp.data.driving.remote.DrbsApi
import com.sygic.driving.testapp.data.driving.remote.utils.QueryConverterFactory
import com.sygic.driving.testapp.data.repository.DrivingRepositoryImpl
import com.sygic.driving.testapp.domain.driving.repository.DrivingRepository
import com.sygic.lib.auth.Auth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSygicAuth(@ApplicationContext context: Context): Auth {
        return SygicAuth.create(context)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat(Constants.ISO_8601_FORMAT)
            .create()
    }

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideDrbsApi(gson: Gson, httpClient: OkHttpClient): DrbsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(DRB_SERVER_DATA_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(QueryConverterFactory.create())
            .client(httpClient)
            .build()

        return retrofit.create(DrbsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDrivingNotificationProvider(@ApplicationContext context: Context): NotificationProvider {
        return NotificationProvider(context)
    }

    @Provides
    @Singleton
    fun provideAppSettings(@ApplicationContext context: Context): AppSettings {
        return AppSettingsImpl(context)
    }

    @Provides
    @Singleton
    fun provideDrivingManager(
        @ApplicationContext context: Context,
        appSettings: AppSettings,
        notificationProvider: NotificationProvider
    ): DrivingManager {
        return DrivingManager(context, appSettings, notificationProvider)
    }

    @Provides
    @Singleton
    fun provideDrivingRepository(drbsApi: DrbsApi, auth: Auth, settings: AppSettings): DrivingRepository {
        return DrivingRepositoryImpl(drbsApi, auth, settings)
    }
}