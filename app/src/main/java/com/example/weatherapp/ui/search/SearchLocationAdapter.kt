package com.example.weatherapp.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.model.SearchHistoryItem

class SearchLocationAdapter(
    private val listener: SearchItemClickListener
) : ListAdapter<SearchHistoryItem, SearchLocationAdapter.SearchViewHolder>(DIFF_CALLBACK) {

    interface SearchItemClickListener {
        fun onItemClick(item: SearchHistoryItem)
        fun onAddClick(item: SearchHistoryItem)
        fun onRemoveClick(item: SearchHistoryItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_history, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationIcon: ImageView = itemView.findViewById(R.id.imageViewLocationIcon)
        private val locationNameText: TextView = itemView.findViewById(R.id.textViewLocationName)
        private val actionButton: ImageButton = itemView.findViewById(R.id.buttonAction)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position))
                }
            }
            
            actionButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item.isCurrentLocation) {
                        listener.onItemClick(item)
                    } else if (actionButton.tag == "add") {
                        listener.onAddClick(item)
                    } else {
                        listener.onRemoveClick(item)
                    }
                }
            }
        }
        
        fun bind(item: SearchHistoryItem) {
            locationNameText.text = item.name
            
            locationIcon.visibility = View.VISIBLE
            
            if (item.isCurrentLocation) {
                actionButton.setImageResource(R.drawable.icon_right_arrow)
                actionButton.tag = "navigate"
            } else {
                actionButton.setImageResource(R.drawable.icon_right_arrow)
                actionButton.tag = "navigate"
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SearchHistoryItem>() {
            override fun areItemsTheSame(oldItem: SearchHistoryItem, newItem: SearchHistoryItem): Boolean {
                return oldItem.name == newItem.name && oldItem.isCurrentLocation == newItem.isCurrentLocation
            }

            override fun areContentsTheSame(oldItem: SearchHistoryItem, newItem: SearchHistoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
} 