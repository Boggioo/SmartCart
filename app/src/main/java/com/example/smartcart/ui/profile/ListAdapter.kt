package com.example.smartcart.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R

class ListAdapter(
    private var lists: MutableList<Map<String, Any>>,
    private val onItemClick: (Int) -> Unit,
    private val onDeleteClick: ((Int, Int) -> Unit)? = null
) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvListName)
        val tvDate: TextView = view.findViewById(R.id.tvListDate)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]
        holder.tvName.text = list["name"] as? String ?: "Lista senza nome"
        holder.tvDate.text = (list["created_at"] as? String)?.substring(0, 10) ?: ""
        
        holder.itemView.setOnClickListener {
            val id = getListId(list)
            if (id != -1) {
                onItemClick(id)
            }
        }
        
        // Configura il pulsante di eliminazione
        if (onDeleteClick != null) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                val id = getListId(list)
                if (id != -1) {
                    onDeleteClick.invoke(id, position)
                }
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }
    
    private fun getListId(list: Map<String, Any>): Int {
        return when (val listId = list["id"]) {
            is Double -> listId.toInt()
            is Int -> listId
            else -> -1
        }
    }

    override fun getItemCount() = lists.size

    fun updateData(newLists: List<Map<String, Any>>) {
        lists = newLists.toMutableList()
        notifyDataSetChanged()
    }
    
    fun removeItem(position: Int) {
        if (position >= 0 && position < lists.size) {
            lists.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}