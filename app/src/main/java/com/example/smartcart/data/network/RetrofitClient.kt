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
        
        // Avvia la ricerca del server in background
        Thread {
            try {
                // Ottieni l'indirizzo IP del server
                val serverUrl = findServerUrl(context)
                Log.d(TAG, "Server URL trovato: $serverUrl")
                
                // Aggiorna l'istanza Retrofit con l'URL trovato
                synchronized(this) {
                    baseUrl = serverUrl
                    retrofit = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(clientBuilder.build())
                        .build()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Errore durante l'inizializzazione di Retrofit: ${e.message}")
            }
        }.start()
        
        // Crea un'istanza Retrofit iniziale con un URL di fallback
        // per evitare che l'app si blocchi durante la ricerca del server
        val fallbackUrl = "http://192.168.1.99:$SERVER_PORT/"
        baseUrl = fallbackUrl
        Log.d(TAG, "URL iniziale (fallback): $baseUrl")
        
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(clientBuilder.build())
            .build()
    }
    
    // Trova l'URL del server tramite scansione della rete locale
    private fun findServerUrl(context: Context): String {
        // Lista di possibili indirizzi IP da provare in ordine di priorità
        val possibleIps = mutableListOf<String>()
        
        // 1. Aggiungi IP prioritari dal server attuale
        possibleIps.add("192.168.1.99")  // IP attuale del server Flask
        possibleIps.add("10.0.2.2")      // Emulatore Android -> localhost del PC host
        possibleIps.add("localhost")     // Localhost
        possibleIps.add("127.0.0.1")     // Localhost alternativo
        
        // 2. Aggiungi l'IP del dispositivo
        getHostIpAddress(context)?.let { deviceIp ->
            possibleIps.add(deviceIp)
            
            // 3. Aggiungi IP nella stessa subnet del dispositivo
            val ipParts = deviceIp.split(".")
            if (ipParts.size == 4) {
                val subnet = "${ipParts[0]}.${ipParts[1]}.${ipParts[2]}"
                // Scansiona gli IP nella stessa subnet
                for (i in 1..254) {
                    possibleIps.add("$subnet.$i")
                }
            }
        }
        
        // 4. Aggiungi IP specifici dai log
        possibleIps.add("192.168.1.251") // IP precedente dai log
        
        // Filtra gli IP null e prova a connettersi a ciascuno
        val validIps = possibleIps.filterNotNull().distinct()
        
        for (ip in validIps) {
            val url = "http://$ip:$SERVER_PORT/"
            Log.d(TAG, "Tentativo di connessione a: $url")
            
            // Verifica se il server Flask è raggiungibile a questo IP
            if (isFlaskServerRunning(ip)) {
                Log.d(TAG, "Server Flask trovato all'indirizzo: $ip")
                return url
            }
        }
        
        // Se nessun IP funziona, usa l'ultimo IP nei log come fallback
        val fallbackIp = "192.168.1.99" // IP attuale del server Flask
        Log.d(TAG, "Nessun server raggiungibile, uso IP di fallback: $fallbackIp")
        return "http://$fallbackIp:$SERVER_PORT/"
    }
    
    // Ottieni l'indirizzo IPv4 del dispositivo
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
            Log.e(TAG, "Errore nell'ottenere l'IP del dispositivo: ${e.message}")
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
    
    // Verifica se il server Flask è in esecuzione su un determinato IP
    private fun isFlaskServerRunning(ipAddress: String): Boolean {
        try {
            // Prima verifica se l'host è raggiungibile
            if (!isServerReachable(ipAddress)) {
                return false
            }
            
            // Crea un client HTTP per verificare se il server Flask risponde
            val client = OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .build()
                
            val request = Request.Builder()
                .url("http://$ipAddress:$SERVER_PORT/")
                .build()
                
            // Esegui la richiesta in modo sincrono (siamo già in un thread separato)
            val response = client.newCall(request).execute()
            
            // Verifica se la risposta è valida
            val isRunning = response.isSuccessful
            
            // Chiudi la risposta per liberare risorse
            response.close()
            
            return isRunning
        } catch (e: Exception) {
            // Se si verifica un'eccezione, il server non è raggiungibile
            Log.d(TAG, "Server non trovato su $ipAddress: ${e.message}")
            return false
        }
    }

    fun api(): ApiService {
        if (retrofit == null) {
            throw IllegalStateException("RetrofitClient non è stato inizializzato. Chiama initialize() prima di usare api().")
        }
        return retrofit!!.create(ApiService::class.java)
    }
}