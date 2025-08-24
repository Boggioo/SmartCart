package com.example.smartcart.data.model

/**
 * Modello dati per rappresentare un supermercato.
 * Utilizzato per visualizzare i supermercati nelle vicinanze sulla mappa.
 * 
 * @property id Identificatore univoco del supermercato
 * @property name Nome del supermercato
 * @property address Indirizzo completo del supermercato
 * @property latitude Latitudine della posizione del supermercato
 * @property longitude Longitudine della posizione del supermercato
 * @property distance Distanza dal punto di riferimento dell'utente in chilometri
 */
data class Supermarket(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double
)
