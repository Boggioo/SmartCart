package com.example.smartcart.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R

class SharedListAdapter(private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<SharedListAdapter.ViewHolder>() {
    
    private var lists: MutableList<Map<String, Any>> = mutableListOf()
    
    fun updateData(newLists: List<Map<String, Any>>) {
        lists.clear()
        lists.addAll(newLists)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shared_list, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]
        holder.bind(list)
    }
    
    override fun getItemCount(): Int = lists.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvListName: TextView = itemView.findViewById(R.id.tvListName)
        private val tvCreator: TextView = itemView.findViewById(R.id.tvCreator)
        
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val list = lists[position]
                    val listId = (list["id"] as Double).toInt()
                    onItemClick(listId)
                }
            }
        }
        
        fun bind(list: Map<String, Any>) {
            tvListName.text = list["name"] as? String ?: "Lista senza nome"
            tvCreator.text = "Creata da: ${list["creator_name"] as? String ?: "Utente sconosciuto"}"
        }
    }
}