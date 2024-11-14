package com.example.appstory.data.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.appstory.data.api.ApiService
import com.example.appstory.data.repository.StoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): StoryDatabase {
        return Room.databaseBuilder(
            context,
            StoryDatabase::class.java,
            "story_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideStoryDao(
        db: StoryDatabase
    ): StoryDao = db.storyDao()

    @Provides
    @Singleton
    fun provideApiService(): ApiService = com.example.appstory.data.api.Retrofit.instance

    @Provides
    @Singleton
    fun provideStoryRepository(
        apiService: ApiService,
        storyDao: StoryDao
    ): StoryRepository {
        return StoryRepository(apiService, storyDao)
    }

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return try {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                "encrypted_preferences",
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("AppModule", "Error creating EncryptedSharedPreferences", e)
            context.getSharedPreferences("fallback_preferences", Context.MODE_PRIVATE)
        }
    }

    @Provides
    @Singleton
    fun provideSessionManager(sharedPreferences: SharedPreferences): SessionManager {
        return SessionManager(sharedPreferences)
    }
}
