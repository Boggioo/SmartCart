package com.example.smartcart.data.model

/**
 * Modello dati per rappresentare un articolo in una lista della spesa.
 * 
 * @property id Identificatore univoco dell'articolo
 * @property name Nome dell'articolo
 * @property quantity Quantità dell'articolo da acquistare
 * @property checked Stato di completamento dell'articolo (acquistato o meno)
 */
data class Item(
    val id: Int,
    val name: String,
    val quantity: Int,
    val checked: Boolean = false
)