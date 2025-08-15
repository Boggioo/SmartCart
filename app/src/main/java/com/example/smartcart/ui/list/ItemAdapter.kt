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
    private var data: MutableList<Item>,
    private val listener: ItemListener
) : RecyclerView.Adapter<ItemAdapter.VH>() {

    interface ItemListener {
        fun onToggleChecked(item: Item, position: Int)
        fun onDelete(item: Item, position: Int)
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkItem)
        val textName: TextView = itemView.findViewById(R.id.textItemName)
        val btnDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.textName.text = "${item.name} (x${item.quantity})"
        holder.checkBox.isChecked = item.checked

        holder.checkBox.setOnClickListener { listener.onToggleChecked(item, position) }
        holder.btnDelete.setOnClickListener { listener.onDelete(item, position) }
    }

    override fun getItemCount(): Int = data.size

    fun setData(newData: MutableList<Item>) {
        data = newData
        notifyDataSetChanged()
    }

    fun addOne(item: Item) {
        data.add(0, item)
        notifyItemInserted(0)
    }

    fun removeItem(item: Item) {
        val idx = data.indexOf(item)
        if (idx >= 0) {
            data.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    fun removeAt(position: Int) {
        if (position in data.indices) {
            data.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateAt(position: Int, item: Item) {
        if (position in data.indices) {
            data[position] = item
            notifyItemChanged(position)
        }
    }

    fun getCheckedItems(): List<Item> {
        return data.filter { it.checked }
    }
}
