package com.example.smartcart.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.model.Item

class ItemAdapter(
    private val data: MutableList<Item>,
    private val listener: ItemListener
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    interface ItemListener {
        fun onToggleChecked(item: Item, position: Int)
        fun onDelete(item: Item, position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkItem)
        val textName: TextView = itemView.findViewById(R.id.textItemName)
        val btnDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.textName.text = "${item.name} (x${item.quantity})"
        holder.checkBox.isChecked = item.checked

        holder.checkBox.setOnClickListener {
            listener.onToggleChecked(item, position)
        }
        holder.btnDelete.setOnClickListener {
            listener.onDelete(item, position)
        }
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: MutableList<Item>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }
}