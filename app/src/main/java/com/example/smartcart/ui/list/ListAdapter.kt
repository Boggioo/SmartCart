package com.example.smartcart.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.model.ShoppingList

/**
 * Adapter per la RecyclerView che gestisce la visualizzazione delle liste della spesa.
 * Permette di visualizzare una lista di ShoppingList e gestire i click sugli elementi.
 * 
 * @param onItemClick Callback chiamato quando viene cliccato un elemento della lista
 */
class ListAdapter(private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
    
    /** Lista delle liste della spesa da visualizzare */
    private var lists: List<ShoppingList> = emptyList()
    
    /**
     * Aggiorna i dati dell'adapter con una nuova lista di ShoppingList.
     * 
     * @param newLists Nuova lista di ShoppingList da visualizzare
     */
    fun updateData(newLists: List<ShoppingList>) {
        lists = newLists
        notifyDataSetChanged()
    }
    
    /**
     * Crea un nuovo ViewHolder inflando il layout per ogni elemento.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }
    
    /**
     * Associa i dati della lista al ViewHolder e configura il listener per i click.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]
        holder.bind(list)
        holder.itemView.setOnClickListener {
            onItemClick(list.id)
        }
    }
    
    /** Restituisce il numero totale di liste */
    override fun getItemCount(): Int = lists.size
    
    /**
     * ViewHolder per contenere i riferimenti alle viste di ogni lista.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /** TextView per il nome della lista */
        private val tvListName: TextView = itemView.findViewById(R.id.tvListName)
        
        /**
         * Associa i dati della lista alle viste.
         * 
         * @param list Lista della spesa da visualizzare
         */
        fun bind(list: ShoppingList) {
            tvListName.text = list.name
        }
    }
}