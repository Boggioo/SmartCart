package com.example.smartcart.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.InetAddress
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Porta del server Flask
    private const val SERVER_PORT = "5000"
    private const val TAG = "RetrofitClient"
    
    // URL di base dinamico che verrà impostato in base all'IP del server
    private var baseUrl = ""
    private var retrofit: Retrofit? = null

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Interceptor personalizzato per log dettagliati
    private val customInterceptor = Interceptor { chain ->
        val request = chain.request()
        
        // Log della richiesta
        Log.d(TAG, "\n--> ${request.method} ${request.url}")
        
        // Esegui la richiesta
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante la richiesta: ${e.message}")
            throw e
        }
        
        // Log della risposta
        Log.d(TAG, "<-- ${response.code} ${response.message} ${response.request.url}")
        
        response
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logger)
        .addInterceptor(customInterceptor)
        .build()
        
    // Configurazione personalizzata di Gson per garantire la corretta serializzazione
    private val gson = GsonBuilder()
        .serializeNulls() // Serializza anche i valori null
        .create()

    // Inizializza il client Retrofit con l'indirizzo IP del server
    fun initialize(context: Context) {
        // Imposta un timeout più lungo per le connessioni
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(customInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
        
        // Ottieni l'indirizzo IP del server
        baseUrl = getServerUrl(context)
        Log.d(TAG, "Server URL: $baseUrl")
        
        // Crea l'istanza Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(clientBuilder.build())
            .build()
    }
    
    // Ottieni l'URL del server
    private fun getServerUrl(context: Context): String {
        // Ottieni l'IPv4 del PC host
        val hostIp = getHostIpAddress(context)
        
        if (hostIp != null) {
            Log.d(TAG, "Usando l'IP del PC host: $hostIp")
            return "http://$hostIp:$SERVER_PORT/"
        }
        
        // Fallback se non è possibile ottenere l'IP del PC host
        val fallbackIp = "192.168.1.99"
        Log.d(TAG, "Impossibile ottenere l'IP del PC host, uso IP di fallback: $fallbackIp")
        return "http://$fallbackIp:$SERVER_PORT/"
    }
    
    // Ottieni l'indirizzo IPv4 del PC host
    private fun getHostIpAddress(context: Context): String? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            
            // Converti l'indirizzo IP da int a formato stringa
            return String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            )
        } catch (e: Exception) {
            Log.e(TAG, "Errore nell'ottenere l'IP del PC host: ${e.message}")
            return null
        }
    }
    
    // Verifica se il server è raggiungibile
    private fun isServerReachable(ipAddress: String): Boolean {
        return try {
            val address = InetAddress.getByName(ipAddress)
            address.isReachable(3000) // timeout di 3 secondi
        } catch (e: Exception) {
            Log.e(TAG, "Errore nel controllo della raggiungibilità del server: ${e.message}")
            false
        }
    }

    fun api(): ApiService {
        if (retrofit == null) {
            throw IllegalStateException("RetrofitClient non è stato inizializzato. Chiama initialize() prima di usare api().")
        }
        return retrofit!!.create(ApiService::class.java)
    }
}