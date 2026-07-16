package com.adgpt.app.di

import android.content.Context
import androidx.room.Room
import com.adgpt.app.BuildConfig
import com.adgpt.app.data.local.ADGPTDatabase
import com.adgpt.app.data.local.ChatMessageDao
import com.adgpt.app.data.network.ChatApi
import com.adgpt.app.data.provider.AiProvider
import com.adgpt.app.data.provider.OfflineAiProvider
import com.adgpt.app.data.repository.ChatRepositoryImpl
import com.adgpt.app.data.repository.SettingsRepositoryImpl
import com.adgpt.app.domain.repository.ChatRepository
import com.adgpt.app.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindings {
    @Binds abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
    @Binds abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds abstract fun bindAiProvider(impl: OfflineAiProvider): AiProvider
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ADGPTDatabase =
        Room.databaseBuilder(context, ADGPTDatabase::class.java, "adgpt.db")
            .build()

    @Provides
    fun provideChatMessageDao(database: ADGPTDatabase): ChatMessageDao = database.chatMessageDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(BuildConfig.ADGPT_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)
}
