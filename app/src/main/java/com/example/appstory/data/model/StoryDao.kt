package com.example.appstory.data.model

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Query("SELECT * FROM stories")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE id = :storyId")
    suspend fun getStoryById(storyId: String): StoryEntity?

    @Query("DELETE FROM stories")
    suspend fun clearAllStories()

    @Delete
    suspend fun deleteStoryById(story: StoryEntity)

    @Query("SELECT * FROM stories")
    fun getPaginatedStories(): PagingSource<Int, StoryEntity>
}