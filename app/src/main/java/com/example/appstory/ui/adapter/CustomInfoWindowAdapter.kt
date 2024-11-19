package com.example.appstory.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.appstory.R
import com.example.appstory.data.model.StoryEntity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)

        val title = view.findViewById<TextView>(R.id.title)
        val snippet = view.findViewById<TextView>(R.id.snippet)
        val image = view.findViewById<ImageView>(R.id.image)

        title.text = marker.title ?: "No Title"
        snippet.text = marker.snippet ?: "No Details Available"

        val story = marker.tag as? StoryEntity
        if (story?.photoUrl != null) {
            Glide.with(context)
                .load(story.photoUrl)
                .transform(CenterCrop(), RoundedCorners(8))
                .placeholder(R.drawable.ic_event_placeholder)
                .error(R.drawable.error_image)
                .into(image)
        } else {
            image.setImageResource(R.drawable.ic_event_placeholder)
        }

        return view
    }
}
