package com.example.smartcart.ui.list

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.ui.profile.ProfileActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartcart.ui.list.SharedListsActivity
import android.widget.TextView

/**
 * Activity principale per la gestione delle liste della spesa.
 * Fornisce funzionalità per creare nuove liste, navigare verso il profilo utente,
 * visualizzare le liste condivise e trovare supermercati vicini.
 */
class ListActivity : AppCompatActivity() {
    /** Gestore della sessione utente */
    private lateinit var session: SessionManager
    /** Pulsante per creare una nuova lista */
    private lateinit var btnCreateList: Button
    /** Pulsante per accedere al profilo utente */
    private lateinit var btnProfile: Button
    /** Pulsante per accedere alle liste condivise */
    private lateinit var btnSharedLists: Button
    /**
     * Metodo chiamato alla creazione dell'activity.
     * Inizializza le viste e configura i listener per i controlli UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        // Inizializza il gestore della sessione
        session = SessionManager(this)
        
        // Inizializzazione delle viste
        btnCreateList = findViewById(R.id.btnCreateList)
        btnProfile = findViewById(R.id.btnProfile)
        btnSharedLists = findViewById(R.id.btnSharedLists)
        
        // Setup click listeners per i bottoni
        setupButtons()
        
        // Gestione del pulsante per accedere alle liste condivise
        btnSharedLists.setOnClickListener {
            // Utilizziamo il percorso completo per evitare ambiguità tra le due classi
            val intent = Intent(this, com.example.smartcart.ui.list.SharedListsActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Configura i listener per tutti i pulsanti dell'interfaccia.
     * Include la navigazione verso le diverse sezioni dell'app.
     */
    private fun setupButtons() {
        // Bottone "Crea nuova lista"
        findViewById<Button>(R.id.btnCreateList).setOnClickListener {
            promptCreateList()
        }

        // Bottone "Trova supermercati vicini"
        findViewById<Button>(R.id.btnNearby).setOnClickListener {
            val intent = Intent(this, com.example.smartcart.ui.map.MapActivity::class.java)
            startActivity(intent)
        }

        // Bottone "Profilo utente"
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Mostra un dialog per l'inserimento del nome di una nuova lista.
     * Valida l'input dell'utente prima di procedere con la creazione.
     */
    private fun promptCreateList() {
        val input = EditText(this).apply {
            hint = "Nome lista"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 0)
            addView(input)
        }
        AlertDialog.Builder(this)
            .setTitle("Crea nuova lista")
            .setView(container)
            .setPositiveButton("Crea") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    android.widget.Toast.makeText(this@ListActivity, "Il nome della lista non può essere vuoto", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                createList(name)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    /**
     * Crea una nuova lista tramite chiamata API.
     * Gestisce l'autenticazione, mostra indicatori di caricamento e
     * naviga verso l'activity della lista creata in caso di successo.
     * 
     * @param name Nome della lista da creare
     */
    private fun createList(name: String) {
        val token = "Bearer ${session.getToken()}"
        
        // Verifica che il token sia valido
        if (session.getToken().isNullOrEmpty()) {
            android.widget.Toast.makeText(this, "Sessione scaduta, effettua nuovamente il login", android.widget.Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, com.example.smartcart.ui.auth.LoginActivity::class.java))
            finish()
            return
        }
        
        // Mostra un indicatore di caricamento
        val progressDialog = android.app.ProgressDialog(this)
        progressDialog.setMessage("Creazione lista in corso...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        
        // Log per debug
        android.util.Log.d("ListActivity", "Creazione lista: $name con token: ${session.getToken()}")
        
        // Utilizziamo una mappa semplice invece di un JSONObject
        val requestMap = mapOf("name" to name)
        
        // Chiamiamo l'API con la mappa
        com.example.smartcart.data.network.RetrofitClient.api()
            .createList(token, requestMap)
            .enqueue(object : retrofit2.Callback<Map<String, Any>> {
                override fun onResponse(
                    call: retrofit2.Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>
                ) {
                    // Nascondi l'indicatore di caricamento
                    progressDialog.dismiss()
                    
                    // Log della risposta per debug
                    android.util.Log.d("ListActivity", "Risposta creazione lista: ${response.code()} - ${response.message()}")
                    
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val responseBody = response.body()!!
                            android.util.Log.d("ListActivity", "Body risposta: $responseBody")
                            
                            val id = when (val idValue = responseBody["id"]) {
                                is Number -> idValue.toInt()
                                is String -> idValue.toInt()
                                else -> throw IllegalStateException("ID non valido: $idValue")
                            }
                            
                            // Mostra un messaggio di successo
                            android.widget.Toast.makeText(
                                this@ListActivity,
                                "Lista creata con successo!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            
                            val intent = Intent(this@ListActivity, CreatedListActivity::class.java)
                            intent.putExtra("list_id", id)
                            startActivity(intent)
                        } catch (e: Exception) {
                            android.util.Log.e("ListActivity", "Errore parsing risposta: ${e.message}", e)
                            android.widget.Toast.makeText(this@ListActivity, "Errore nel formato dei dati: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Ottieni il corpo dell'errore se disponibile
                        var errorBody = ""
                        try {
                            errorBody = response.errorBody()?.string() ?: ""
                            android.util.Log.e("ListActivity", "Error body: $errorBody")
                            
                            // Analisi dettagliata dell'errore
                            android.util.Log.e("ListActivity", "Codice errore: ${response.code()}")
                            android.util.Log.e("ListActivity", "Messaggio errore: ${response.message()}")
                            
                            // Proviamo a parsare il JSON dell'errore
                            try {
                                val jsonError = org.json.JSONObject(errorBody)
                                android.util.Log.e("ListActivity", "JSON Error: $jsonError")
                            } catch (jsonEx: Exception) {
                                android.util.Log.e("ListActivity", "Errore parsing JSON errore: ${jsonEx.message}")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ListActivity", "Errore lettura error body: ${e.message}")
                        }
                        
                        // Mostra un messaggio di errore più specifico basato sul codice di errore
                        val errorMessage = when (response.code()) {
                            400 -> "Dati non validi. Verifica il nome della lista."
                            401 -> "Sessione scaduta. Effettua nuovamente il login."
                            422 -> "Il nome della lista non è valido. Assicurati che non sia vuoto."
                            500 -> "Errore del server. Riprova più tardi."
                            else -> "Errore creazione lista (${response.code()}): $errorBody"
                        }
                        
                        android.widget.Toast.makeText(
                            this@ListActivity, 
                            errorMessage, 
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                    // Nascondi l'indicatore di caricamento
                    progressDialog.dismiss()
                    
                    android.util.Log.e("ListActivity", "Errore di rete: ${t.message}", t)
                    android.widget.Toast.makeText(this@ListActivity, "Errore di connessione: controlla la tua connessione internet e riprova", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
    }
}