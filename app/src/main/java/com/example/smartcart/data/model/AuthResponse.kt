package com.example.smartcart.data.model

/**
 * Modello di risposta per le operazioni di autenticazione (login e registrazione).
 * Contiene i dati restituiti dal server dopo un'autenticazione riuscita.
 * 
 * @param access_token Token JWT per l'autenticazione delle richieste API
 * @param user_id ID univoco dell'utente autenticato
 */
data class AuthResponse(
    val access_token: String,
    val user_id: Int
)
