package com.example.appstory.ui.storylist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appstory.data.model.StoryEntity
import com.example.appstory.databinding.ActivityMainBinding
import com.example.appstory.ui.AuthViewModel
import com.example.appstory.ui.StoryViewModel
import com.example.appstory.ui.adapter.StoryAdapter
import com.example.appstory.ui.addstory.AddStoryActivity
import com.example.appstory.ui.login.LoginActivity
import com.example.appstory.ui.storydetail.StoryDetailActivity
import com.example.appstory.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoryListActivity : AppCompatActivity() {
    private val storyViewModel: StoryViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "MainActivity onCreate executed")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        storyAdapter = StoryAdapter(emptyList()) { story ->
            navigateToDetail(story)
        }

        binding.apply {
            rvStories.layoutManager = LinearLayoutManager(this@StoryListActivity)
            rvStories.adapter = storyAdapter

            actionLogout.setOnClickListener {
                authViewModel.logout()
            }

            fabAdd.setOnClickListener {
                startActivity(Intent(this@StoryListActivity, AddStoryActivity::class.java))
            }
        }
    }

    private fun setupObservers() {
        storyViewModel.storyList.observe(this) { resource ->
            handleStoryListResource(resource)
        }

        authViewModel.authState.observe(this) { resource ->
            handleAuthState(resource)
        }
    }

    private fun fetchStories() {
        val page = 1
        val size = 10
        storyViewModel.getAllStories(page, size)
    }

    private fun handleStoryListResource(resource: Resource<List<StoryEntity>>) {
        when (resource) {
            is Resource.Loading -> showLoading(true)
            is Resource.Success -> {
                showLoading(false)
                resource.data?.let { stories ->
                    storyAdapter.updateStories(stories)
                }
            }
            is Resource.Error -> {
                showLoading(false)
                showError(resource.message)
            }
        }
    }

    private fun handleAuthState(resource: Resource<String?>) {
        when (resource) {
            is Resource.Loading -> {
                showLoading(true)
            }
            is Resource.Success -> {
                showLoading(false)
                val token = resource.data
                if (token != null) {
                    fetchStories()
                } else {
                    navigateToLogin()
                }
            }
            is Resource.Error -> {
                showLoading(false)
                showError(resource.message)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String?) {
        Toast.makeText(this, message ?: "Unknown error occurred", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToDetail(story: StoryEntity) {
        Log.d("MainActivity", "Navigating to detail with story id: ${story.id}")
        val intent = Intent(this, StoryDetailActivity::class.java).apply {
            putExtra("STORY_ID", story.id)
        }
        startActivity(intent)
    }


    @Suppress("DEPRECATION")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}