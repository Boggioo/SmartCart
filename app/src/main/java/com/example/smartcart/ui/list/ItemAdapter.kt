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
    private var _data: MutableList<Item>,
    private val listener: ItemListener
) : RecyclerView.Adapter<ItemAdapter.VH>() {

    interface ItemListener {
        fun onToggleChecked(item: Item, position: Int)
        fun onDelete(item: Item, position: Int)
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkItem)
        val textName: TextView = itemView.findViewById(R.id.textItemName)
        val textQuantity: TextView = itemView.findViewById(R.id.textQuantity)
        val btnDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = _data[position]
        holder.textName.text = item.name
        holder.textQuantity.text = "x${item.quantity}"
        holder.checkBox.isChecked = item.checked

        holder.checkBox.setOnClickListener { listener.onToggleChecked(item, position) }
        holder.btnDelete.setOnClickListener { listener.onDelete(item, position) }
    }

    override fun getItemCount(): Int = _data.size

    fun updateData(newData: MutableList<Item>) {
        _data = newData
        notifyDataSetChanged()
    }

    fun addOne(item: Item) {
        _data.add(0, item)
        notifyItemInserted(0)
    }

    fun removeItem(item: Item) {
        val idx = _data.indexOf(item)
        if (idx >= 0) {
            _data.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    fun removeAt(position: Int) {
        if (position in _data.indices) {
            _data.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateAt(position: Int, item: Item) {
        if (position in _data.indices) {
            _data[position] = item
            notifyItemChanged(position)
        }
    }

    fun getCheckedItems(): List<Item> {
        return _data.filter { it.checked }
    }

    // Metodi aggiunti per accedere ai dati
    fun getItemAtPosition(position: Int): Item {
        return _data[position]
    }

    fun getData(): List<Item> {
        return _data
    }
}