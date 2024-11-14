package com.example.appstory.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appstory.R
import com.example.appstory.data.model.StoryEntity
import javax.inject.Inject

class StoryAdapter @Inject constructor(
    private var stories: List<StoryEntity>,
    private val onItemClick: (StoryEntity) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storyTitle: TextView = itemView.findViewById(R.id.story_title)
        val storyDescription: TextView = itemView.findViewById(R.id.story_description)
        val storyImage: ImageView = itemView.findViewById(R.id.story_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        holder.storyTitle.text = story.name
        holder.storyDescription.text = story.description

        Glide.with(holder.itemView.context)
            .load(story.photoUrl)
            .placeholder(R.drawable.ic_event_placeholder)
            .error(R.drawable.error_image)
            .into(holder.storyImage)

        holder.itemView.setOnClickListener {
            onItemClick(story)
        }
    }

    override fun getItemCount(): Int {
        return stories.size
    }

    fun updateStories(newStories: List<StoryEntity>) {
        val diffCallback = StoryDiffCallback(stories, newStories)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        stories = newStories
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

    class StoryDiffCallback(
        private val oldList: List<StoryEntity>,
        private val newList: List<StoryEntity>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
