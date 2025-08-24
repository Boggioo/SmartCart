package com.example.smartcart

import android.app.Application
import android.util.Log
import com.example.smartcart.data.network.RetrofitClient

/**
 * Classe principale dell'applicazione SmartCart.
 * Estende Application per gestire l'inizializzazione globale dei componenti.
 */
class SmartCartApp : Application() {
    
    /**
     * Metodo chiamato alla creazione dell'applicazione.
     * Inizializza i componenti globali necessari per il funzionamento dell'app.
     */
    override fun onCreate() {
        super.onCreate()
        
        // Inizializza RetrofitClient all'avvio dell'applicazione
        try {
            RetrofitClient.initialize(applicationContext)
            Log.d("SmartCartApp", "RetrofitClient inizializzato con successo")
        } catch (e: Exception) {
            Log.e("SmartCartApp", "Errore durante l'inizializzazione di RetrofitClient: ${e.message}")
        }
    }
}
