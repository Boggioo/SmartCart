package com.example.smartcart.data.model

/**
 * Modello dati per rappresentare una lista della spesa.
 * 
 * @property id Identificatore univoco della lista
 * @property name Nome della lista della spesa
 * @property userId ID dell'utente proprietario della lista
 * @property createdAt Data e ora di creazione della lista in formato stringa
 */
data class ShoppingList(
    val id: Int,
    val name: String,
    val userId: Int,
    val createdAt: String
)