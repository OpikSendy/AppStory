package com.example.appstory.ui.storydetail

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.appstory.R
import com.example.appstory.data.model.Story
import com.example.appstory.data.model.toStory
import com.example.appstory.databinding.ActivityStoryDetailBinding
import com.example.appstory.ui.StoryViewModel
import com.example.appstory.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoryDetailActivity : AppCompatActivity() {

    private var _binding: ActivityStoryDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logIntent()
        setupToolbar()
        handleIntent()
        observeStoryDetail()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.story_detail)
        }
    }

    private fun handleIntent() {
        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        if (storyId != null) {
            viewModel.getStoryDetail(storyId)
        } else {
            showError("Story ID is missing")
            finish()
        }
    }

    private fun observeStoryDetail() {
        viewModel.storyDetail.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { storyEntity ->
                        displayStoryDetails(storyEntity.toStory())
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    Log.e("StoryDetailActivity", "Error: ${resource.message}")
                    showError(resource.message ?: "Unknown error occurred")
                }
            }
        }
    }

    private fun displayStoryDetails(story: Story) {
        Log.d("StoryDetailActivity", "Displaying story: $story")

        with(binding) {
            tvDetailName.text = story.name ?: ""
            tvDetailDescription.text = story.description ?: ""

            if (!story.photoUrl.isNullOrEmpty()) {
                Glide.with(this@StoryDetailActivity)
                    .load(story.photoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_event_placeholder)
                    .error(R.drawable.error_image)
                    .into(ivDetailPhoto)
            } else {
                ivDetailPhoto.setImageResource(R.drawable.ic_event_placeholder)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun logIntent() {
        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        Log.d("StoryDetailActivity", "Intent received with story ID: $storyId")
    }

    companion object {
        const val EXTRA_STORY_ID = "STORY_ID"
    }
}