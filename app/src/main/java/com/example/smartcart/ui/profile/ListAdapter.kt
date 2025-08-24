package com.example.smartcart.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R

/**
 * Adapter per la RecyclerView che gestisce la visualizzazione delle liste.
 * Supporta il click sugli elementi e l'eliminazione opzionale delle liste.
 */
class ListAdapter(
    /** Lista mutabile delle liste da visualizzare */
    private var lists: MutableList<Map<String, Any>>,
    /** Callback per il click su un elemento della lista */
    private val onItemClick: (Int) -> Unit,
    /** Callback opzionale per l'eliminazione di una lista */
    private val onDeleteClick: ((Int, Int) -> Unit)? = null
) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    /**
     * ViewHolder che contiene i riferimenti alle viste di ogni elemento della lista.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /** TextView per il nome della lista */
        val tvName: TextView = view.findViewById(R.id.tvListName)
        /** TextView per la data di creazione della lista */
        val tvDate: TextView = view.findViewById(R.id.tvListDate)
        /** Pulsante per eliminare la lista */
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteList)
    }

    /**
     * Crea un nuovo ViewHolder inflando il layout per ogni elemento della lista.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    /**
     * Associa i dati di una lista specifica alle viste del ViewHolder.
     * Configura i listener per il click e l'eliminazione.
     */
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
    
    /**
     * Estrae l'ID della lista gestendo diversi tipi di dato.
     * @param list Mappa contenente i dati della lista
     * @return ID della lista come intero, -1 se non valido
     */
    private fun getListId(list: Map<String, Any>): Int {
        return when (val listId = list["id"]) {
            is Double -> listId.toInt()
            is Int -> listId
            else -> -1
        }
    }

    /**
     * Restituisce il numero totale di elementi nella lista.
     */
    override fun getItemCount() = lists.size

    /**
     * Aggiorna i dati dell'adapter con una nuova lista.
     * @param newLists Nuova lista di dati da visualizzare
     */
    fun updateData(newLists: List<Map<String, Any>>) {
        lists = newLists.toMutableList()
        notifyDataSetChanged()
    }
    
    /**
     * Rimuove un elemento dalla lista in una posizione specifica.
     * @param position Posizione dell'elemento da rimuovere
     */
    fun removeItem(position: Int) {
        if (position >= 0 && position < lists.size) {
            lists.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}