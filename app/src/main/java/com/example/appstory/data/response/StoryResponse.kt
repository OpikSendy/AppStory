package com.example.appstory.data.response

import com.example.appstory.data.model.Story
import com.google.gson.annotations.SerializedName

data class StoryResponse(
    @field:SerializedName("error") val error: Boolean,
    @field:SerializedName("message") val message: String,
    @field:SerializedName("listStory") val listStory: List<Story>
)