package com.example.smartcart

import android.app.Application
import android.util.Log
import com.example.smartcart.data.network.RetrofitClient

class SmartCartApp : Application() {
    
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
