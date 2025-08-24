package com.example.smartcart.data.model

/**
 * Modello dati per le richieste di creazione di nuovi articoli.
 * Utilizzato per inviare dati al backend quando si aggiunge un articolo a una lista.
 * 
 * @property name Nome dell'articolo da aggiungere
 * @property quantity Quantità dell'articolo
 * @property list_id ID della lista a cui aggiungere l'articolo
 */
data class ItemRequest(
    val name: String,
    val quantity: Int,
    val list_id: Int
)
