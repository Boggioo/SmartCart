package com.example.smartcart.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.model.Supermarket

class SupermarketAdapter(
    private var data: MutableList<Supermarket>
) : RecyclerView.Adapter<SupermarketAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textSupermarketName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.supermarket_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.textName.text = "${item.name} - ${"%.2f".format(item.distance)} km"
    }

    override fun getItemCount(): Int = data.size

    fun setData(newData: MutableList<Supermarket>) {
        data = newData
        notifyDataSetChanged()
    }
}