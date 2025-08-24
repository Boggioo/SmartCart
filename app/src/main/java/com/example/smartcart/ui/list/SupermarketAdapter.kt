package com.example.smartcart.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.model.Supermarket

/**
 * Adapter per la RecyclerView che gestisce la visualizzazione dei supermercati.
 * Mostra una lista di supermercati con nome e distanza dalla posizione corrente.
 * 
 * @param data Lista mutabile dei supermercati da visualizzare
 */
class SupermarketAdapter(
    private var data: MutableList<Supermarket>
) : RecyclerView.Adapter<SupermarketAdapter.VH>() {

    /**
     * ViewHolder per contenere i riferimenti alle viste di ogni supermercato.
     */
    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /** TextView per il nome del supermercato e la distanza */
        val textName: TextView = itemView.findViewById(R.id.textSupermarketName)
    }

    /**
     * Crea un nuovo ViewHolder inflando il layout per ogni elemento.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.supermarket_row, parent, false)
        return VH(v)
    }

    /**
     * Associa i dati del supermercato al ViewHolder.
     * Mostra il nome del supermercato e la distanza formattata.
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = data[position]
        holder.textName.text = "${item.name} - ${"%.2f".format(item.distance)} km"
    }

    /** Restituisce il numero totale di supermercati */
    override fun getItemCount(): Int = data.size

    /**
     * Aggiorna i dati dell'adapter con una nuova lista di supermercati.
     * 
     * @param newData Nuova lista di supermercati
     */
    fun setData(newData: MutableList<Supermarket>) {
        data = newData
        notifyDataSetChanged()
    }
}