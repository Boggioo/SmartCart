package com.example.smartcart.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.model.ShoppingList

class ListAdapter(private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    
    private var lists: List<ShoppingList> = emptyList()
    
    fun updateData(newLists: List<ShoppingList>) {
        lists = newLists
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]
        holder.bind(list)
        holder.itemView.setOnClickListener {
            onItemClick(list.id)
        }
    }
    
    override fun getItemCount(): Int = lists.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvListName: TextView = itemView.findViewById(R.id.tvListName)
        
        fun bind(list: ShoppingList) {
            tvListName.text = list.name
        }
    }
}