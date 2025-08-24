package com.example.smartcart.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestore delle sessioni utente utilizzando SharedPreferences.
 * Gestisce il salvataggio e il recupero dei dati di autenticazione dell'utente.
 */
class SessionManager(ctx: Context) {
    /** Istanza di SharedPreferences per la persistenza dei dati di sessione */
    private val prefs: SharedPreferences =
        ctx.getSharedPreferences("smartcart_prefs", Context.MODE_PRIVATE)

    /** Salva il token di autenticazione JWT */
    fun saveToken(token: String) { prefs.edit().putString("token", token).apply() }
    
    /** Recupera il token di autenticazione salvato */
    fun getToken(): String? = prefs.getString("token", null)

    /** Salva l'ID dell'utente autenticato */
    fun saveUserId(id: Int) { prefs.edit().putInt("user_id", id).apply() }
    
    /** Recupera l'ID dell'utente salvato */
    fun getUserId(): Int = prefs.getInt("user_id", -1)

    /** Salva il nome dell'utente autenticato */
    fun saveUserName(name: String) { prefs.edit().putString("user_name", name).apply() }
    
    /** Recupera il nome dell'utente salvato */
    fun getUserName(): String? = prefs.getString("user_name", "Utente")

    /** Cancella tutti i dati di sessione salvati */
    fun clear() { prefs.edit().clear().apply() }

    /** Verifica se l'utente è attualmente autenticato */
    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty() && getUserId() != -1
    }
}