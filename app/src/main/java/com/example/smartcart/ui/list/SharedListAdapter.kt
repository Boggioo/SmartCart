package com.example.smartcart.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R

/**
 * Adapter per la RecyclerView che gestisce la visualizzazione delle liste condivise.
 * Mostra le liste della spesa condivise da altri utenti con informazioni sul creatore.
 * 
 * @param onItemClick Callback chiamato quando viene cliccato un elemento della lista
 */
class SharedListAdapter(private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<SharedListAdapter.ViewHolder>() {
    
    /** Lista delle liste condivise da visualizzare */
    private var lists: MutableList<Map<String, Any>> = mutableListOf()
    
    /**
     * Aggiorna i dati dell'adapter con una nuova lista di liste condivise.
     * 
     * @param newLists Nuova lista di liste condivise da visualizzare
     */
    fun updateData(newLists: List<Map<String, Any>>) {
        lists.clear()
        lists.addAll(newLists)
        notifyDataSetChanged()
    }
    
    /**
     * Crea un nuovo ViewHolder inflando il layout per ogni elemento.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shared_list, parent, false)
        return ViewHolder(view)
    }
    
    /**
     * Associa i dati della lista condivisa al ViewHolder.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]
        holder.bind(list)
    }
    
    /** Restituisce il numero totale di liste condivise */
    override fun getItemCount(): Int = lists.size
    
    /**
     * ViewHolder per contenere i riferimenti alle viste di ogni lista condivisa.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /** TextView per il nome della lista */
        private val tvListName: TextView = itemView.findViewById(R.id.tvListName)
        /** TextView per il nome del creatore della lista */
        private val tvCreator: TextView = itemView.findViewById(R.id.tvCreator)
        
        /**
         * Inizializza il ViewHolder configurando il listener per i click.
         */
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
        
        /**
         * Associa i dati della lista condivisa alle viste.
         * 
         * @param list Mappa contenente i dati della lista condivisa
         */
        fun bind(list: Map<String, Any>) {
            tvListName.text = list["name"] as? String ?: "Lista senza nome"
            tvCreator.text = "Creata da: ${list["creator_name"] as? String ?: "Utente sconosciuto"}"
        }
    }
}