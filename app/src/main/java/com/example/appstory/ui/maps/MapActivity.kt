package com.example.appstory.ui.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.appstory.R
import com.example.appstory.data.model.StoryEntity
import com.example.appstory.databinding.ActivityMapBinding
import com.example.appstory.ui.MapViewModel
import com.example.appstory.ui.login.LoginActivity
import com.example.appstory.utils.Resource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private val viewModel: MapViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupMap()
        setupObserver()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.story_location)
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupObserver() {
        viewModel.storiesWithLocation.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    result.data?.let { stories ->
                        showMarkers(stories)
                    }
                }
                is Resource.Loading -> showLoading()
                is Resource.Error -> {
                    hideLoading()
                    showError(result.message ?: getString(R.string.unknown_error))
                }
            }
        }

        viewModel.mapState.observe(this) { state ->
            when (state) {
                MapViewModel.MapState.UNAUTHORIZED -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                MapViewModel.MapState.EMPTY -> {
                    showEmpty()
                }
                MapViewModel.MapState.ERROR -> {
                    binding.errorView.visibility = View.VISIBLE
                }
                else -> {
                    binding.errorView.visibility = View.GONE
                }
            }
        }
    }

    private fun getMyLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (googleMap == null) {
            Log.e(TAG, "GoogleMap is null")
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show()
            return
        }

        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
            isMyLocationButtonEnabled = true
        }

        getMyLocation()
        viewModel.refreshStories()
    }

    private fun showMarkers(stories: List<StoryEntity>) {
        mMap.clear()
        stories.forEach { story ->
            story.lat?.let { lat ->
                story.lon?.let { lon ->
                    val latLng = LatLng(lat, lon)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(story.name)
                            .snippet(story.description)
                    )?.tag = story
                }
            }
        }

        if (stories.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            stories.forEach { story ->
                story.lat?.let { lat ->
                    story.lon?.let { lon ->
                        boundsBuilder.include(LatLng(lat, lon))
                    }
                }
            }
            val bounds = boundsBuilder.build()
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showEmpty() {
        binding.emptyView.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.errorView.visibility = View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MapActivity"
    }

}
