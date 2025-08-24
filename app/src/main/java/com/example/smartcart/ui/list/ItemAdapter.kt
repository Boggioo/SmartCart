package com.example.smartcart.ui.list

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.model.Item

/**
 * Adapter per la RecyclerView che gestisce la visualizzazione degli elementi della lista.
 * Supporta sia gli elementi da completare che quelli completati, con stili diversi.
 * 
 * @param data Lista mutabile degli elementi da visualizzare
 * @param listener Listener per gestire le azioni sugli elementi
 * @param isCompletedList Flag che indica se si tratta della lista degli elementi completati
 */
class ItemAdapter(
    private val data: MutableList<Item>,
    private val listener: ItemListener,
    private val isCompletedList: Boolean = false
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    /**
     * Interfaccia per gestire le azioni sugli elementi della lista.
     */
    interface ItemListener {
        /** Chiamato quando viene cambiato lo stato di completamento di un elemento */
        fun onToggleChecked(item: Item, position: Int)
        /** Chiamato quando viene richiesta l'eliminazione di un elemento */
        fun onDelete(item: Item, position: Int)
    }

    /**
     * ViewHolder per contenere i riferimenti alle viste di ogni elemento.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /** CheckBox per indicare lo stato di completamento */
        val checkBox: CheckBox = itemView.findViewById(R.id.checkItem)
        /** TextView per il nome e la quantità dell'elemento */
        val textName: TextView = itemView.findViewById(R.id.textItemName)
        /** Pulsante per eliminare l'elemento */
        val btnDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
    }

    /**
     * Crea un nuovo ViewHolder inflando il layout per ogni elemento.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return ViewHolder(view)
    }

    /**
     * Associa i dati dell'elemento alle viste del ViewHolder.
     * Applica stili diversi per elementi completati e da completare.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.textName.text = "${item.name} (x${item.quantity})"
        holder.checkBox.isChecked = item.checked
        
        // Applica il testo barrato se l'elemento è nella lista dei completati
        if (isCompletedList) {
            holder.textName.paintFlags = holder.textName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.textName.paintFlags = holder.textName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.checkBox.setOnClickListener {
            listener.onToggleChecked(item, position)
        }
        holder.btnDelete.setOnClickListener {
            listener.onDelete(item, position)
        }
    }

    /** Restituisce il numero totale di elementi nella lista */
    override fun getItemCount(): Int = data.size

    /**
     * Aggiorna completamente i dati dell'adapter.
     * 
     * @param newData Nuova lista di elementi
     */
    fun updateData(newData: MutableList<Item>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    /**
     * Rimuove un elemento dalla posizione specificata.
     * 
     * @param position Posizione dell'elemento da rimuovere
     */
    fun removeAt(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * Aggiunge un nuovo elemento all'inizio della lista.
     * 
     * @param item Elemento da aggiungere
     */
    fun addItem(item: Item) {
        data.add(0, item)
        notifyItemInserted(0)
    }

    /**
     * Aggiorna un elemento alla posizione specificata.
     * 
     * @param position Posizione dell'elemento da aggiornare
     * @param item Nuovo elemento
     */
    fun updateAt(position: Int, item: Item) {
        data[position] = item
        notifyItemChanged(position)
    }
}