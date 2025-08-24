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

/**
 * Client Retrofit per la gestione delle comunicazioni HTTP con il backend Flask.
 * Implementa la scoperta automatica del server sulla rete locale e gestisce
 * la configurazione dinamica dell'URL di base.
 */
object RetrofitClient {
    /** Porta del server Flask backend */
    private const val SERVER_PORT = "5000"
    /** Tag per i log di debug */
    private const val TAG = "RetrofitClient"
    
    /** URL di base dinamico che viene impostato in base all'IP del server scoperto */
    private var baseUrl = ""
    /** Istanza Retrofit per le chiamate API */
    private var retrofit: Retrofit? = null

    /** Interceptor per il logging delle richieste HTTP */
    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /** Interceptor personalizzato per log dettagliati delle richieste e risposte */
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
        .addInterceptor { chain ->
            // Disabilita la cache per tutte le richieste
            val request = chain.request().newBuilder()
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .cache(null) // Disabilita completamente la cache
        .build()
        
    // Configurazione personalizzata di Gson per garantire la corretta serializzazione
    private val gson = GsonBuilder()
        .serializeNulls() // Serializza anche i valori null
        .create()

    /**
     * Inizializza il client Retrofit con scoperta automatica del server Flask.
     * Avvia la ricerca del server in background e configura un URL di fallback temporaneo.
     * 
     * @param context Contesto dell'applicazione per accedere ai servizi di sistema
     */
    fun initialize(context: Context) {
        // Imposta un timeout più lungo per le connessioni
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(customInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
        
        // Avvia la ricerca del server in background
        Thread {
            try {
                // Scopri automaticamente l'IP del server Flask
                val serverUrl = discoverFlaskServer(context)
                Log.d(TAG, "Server Flask scoperto: $serverUrl")
                
                // Aggiorna l'istanza Retrofit con l'URL trovato
                synchronized(this) {
                    baseUrl = serverUrl
                    retrofit = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(clientBuilder.build())
                        .build()
                    Log.d(TAG, "Retrofit aggiornato con nuovo URL: $baseUrl")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Errore durante la scoperta del server: ${e.message}")
            }
        }.start()
        
        // Crea un'istanza Retrofit iniziale con un URL di fallback temporaneo
        // per evitare che l'app si blocchi durante la ricerca del server
        val fallbackUrl = "http://127.0.0.1:$SERVER_PORT/"
        baseUrl = fallbackUrl
        Log.d(TAG, "URL iniziale (temporaneo): $baseUrl")
        
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(clientBuilder.build())
            .build()
    }
    
    /**
     * Scopre automaticamente l'IP del server Flask scansionando la rete locale.
     * Prova prima gli IP standard, poi scansiona la subnet del dispositivo.
     * 
     * @param context Contesto per accedere ai servizi di rete
     * @return URL completo del server Flask scoperto
     */
    private fun discoverFlaskServer(context: Context): String {
        Log.d(TAG, "Avvio scoperta automatica del server Flask...")
        
        // 1. Prima prova gli IP standard
        val standardIps = listOf(
            "10.0.2.2",      // Android emulator localhost
            "127.0.0.1",     // Localhost IP
            "localhost"       // Localhost standard
        )
        
        for (ip in standardIps) {
            Log.d(TAG, "Provo IP standard: $ip:$SERVER_PORT")
            if (isFlaskServerRunning(ip)) {
                val serverUrl = "http://$ip:$SERVER_PORT/"
                Log.d(TAG, "Server Flask trovato su IP standard: $serverUrl")
                return serverUrl
            }
        }
        
        // 2. Ottieni l'IP del dispositivo e scansiona la sua subnet
         val deviceIp = getDeviceIpAddress(context)
         if (deviceIp != null) {
             Log.d(TAG, "IP del dispositivo: $deviceIp")
             val subnet = getSubnetFromIp(deviceIp)
             Log.d(TAG, "Scansione subnet: $subnet")
             
             // Scansiona gli IP più comuni nella subnet (inclusi quelli dai log del server)
             val commonLastOctets = listOf(1, 99, 100, 101, 102, 150, 200, 251, 254)
             for (lastOctet in commonLastOctets) {
                 val testIp = "$subnet.$lastOctet"
                 Log.d(TAG, "Provo IP subnet: $testIp:$SERVER_PORT")
                 if (isFlaskServerRunning(testIp)) {
                     val serverUrl = "http://$testIp:$SERVER_PORT/"
                     Log.d(TAG, "Server Flask trovato nella subnet: $serverUrl")
                     return serverUrl
                 }
             }
         }
         
         // 3. Prova IP specifici dai log del server come ultima risorsa
         val logIps = listOf("192.168.1.251", "192.168.1.99")
         for (ip in logIps) {
             Log.d(TAG, "Provo IP dai log: $ip:$SERVER_PORT")
             if (isFlaskServerRunning(ip)) {
                 val serverUrl = "http://$ip:$SERVER_PORT/"
                 Log.d(TAG, "Server Flask trovato dai log: $serverUrl")
                 return serverUrl
             }
         }
        
        // 4. Se non trovato, usa l'IP di fallback
         val fallbackUrl = "http://127.0.0.1:$SERVER_PORT/"
         Log.w(TAG, "Server Flask non trovato automaticamente, uso fallback: $fallbackUrl")
         return fallbackUrl
    }
    
    /**
     * Ottiene l'indirizzo IPv4 del dispositivo Android tramite WiFiManager.
     * 
     * @param context Contesto per accedere al WiFiManager
     * @return Indirizzo IP del dispositivo o null se non disponibile
     */
    private fun getDeviceIpAddress(context: Context): String? {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipInt = wifiInfo.ipAddress
            
            // Converte l'IP da intero a stringa
            val ip = String.format(
                "%d.%d.%d.%d",
                (ipInt and 0xff),
                (ipInt shr 8 and 0xff),
                (ipInt shr 16 and 0xff),
                (ipInt shr 24 and 0xff)
            )
            
            Log.d(TAG, "IP del dispositivo Android: $ip")
            ip
        } catch (e: Exception) {
            Log.e(TAG, "Errore nell'ottenere l'IP del dispositivo: ${e.message}")
            null
        }
    }
    
    /**
     * Estrae la subnet (primi tre ottetti) da un indirizzo IP.
     * 
     * @param ip Indirizzo IP completo
     * @return Subnet nel formato "xxx.xxx.xxx"
     */
    private fun getSubnetFromIp(ip: String): String {
        val parts = ip.split(".")
        return if (parts.size >= 3) {
            "${parts[0]}.${parts[1]}.${parts[2]}"
        } else {
            "192.168.1" // Fallback subnet
        }
    }
    
    /**
     * Funzione di compatibilità per ottenere l'IP del dispositivo.
     * 
     * @param context Contesto dell'applicazione
     * @return Indirizzo IP del dispositivo
     */
    private fun getHostIpAddress(context: Context): String? {
        return getDeviceIpAddress(context)
    }
    
    /**
     * Verifica se un server è raggiungibile tramite ping ICMP.
     * 
     * @param ipAddress Indirizzo IP da testare
     * @return true se il server è raggiungibile, false altrimenti
     */
    private fun isServerReachable(ipAddress: String): Boolean {
        return try {
            val address = InetAddress.getByName(ipAddress)
            address.isReachable(3000) // timeout di 3 secondi
        } catch (e: Exception) {
            Log.e(TAG, "Errore nel controllo della raggiungibilità del server: ${e.message}")
            false
        }
    }
    
    /**
     * Verifica se il server Flask è in esecuzione su un determinato IP
     * effettuando una richiesta HTTP diretta.
     * 
     * @param ipAddress Indirizzo IP da testare
     * @return true se il server Flask risponde correttamente, false altrimenti
     */
    private fun isFlaskServerRunning(ipAddress: String): Boolean {
        return try {
            // Prova a fare una connessione HTTP diretta al server Flask
            val url = "http://$ipAddress:$SERVER_PORT/"
            val client = OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build()
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            response.close()
            
            // Verifica se la risposta contiene indicatori del server Flask
            val isFlaskServer = response.isSuccessful && 
                               (responseBody?.contains("status") == true || 
                                responseBody?.contains("ok") == true ||
                                response.code == 200)
            
            if (isFlaskServer) {
                Log.d(TAG, "✓ Server Flask TROVATO su $ipAddress:$SERVER_PORT")
            } else {
                Log.d(TAG, "✗ Server Flask NON TROVATO su $ipAddress:$SERVER_PORT (code: ${response.code})")
            }
            
            isFlaskServer
        } catch (e: Exception) {
            Log.d(TAG, "✗ Server Flask su $ipAddress:$SERVER_PORT NON RAGGIUNGIBILE: ${e.message}")
            false
        }
    }

    /**
     * Restituisce un'istanza del servizio API per effettuare chiamate HTTP.
     * 
     * @return Istanza di ApiService configurata
     * @throws IllegalStateException se RetrofitClient non è stato inizializzato
     */
    fun api(): ApiService {
        if (retrofit == null) {
            throw IllegalStateException("RetrofitClient non è stato inizializzato. Chiama initialize() prima di usare api().")
        }
        return retrofit!!.create(ApiService::class.java)
    }
}