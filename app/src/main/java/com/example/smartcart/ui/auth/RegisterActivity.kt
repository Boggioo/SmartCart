package com.example.smartcart.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.data.network.RetrofitClient
import com.example.smartcart.ui.list.ListActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity per la gestione della registrazione di nuovi utenti.
 * Permette agli utenti di creare un nuovo account fornendo nome, email e password,
 * gestisce la validazione dei campi e la navigazione verso il login.
 */
class RegisterActivity : AppCompatActivity() {
    /** Campo di input per il nome dell'utente */
    private lateinit var etName: EditText
    /** Campo di input per l'email dell'utente */
    private lateinit var etEmail: EditText
    /** Campo di input per la password dell'utente */
    private lateinit var etPassword: EditText
    /** Pulsante per effettuare la registrazione */
    private lateinit var btnRegister: Button
    /** TextView per il login (non utilizzato) */
    private lateinit var tvLogin: TextView
    /** Gestore della sessione utente */
    private lateinit var session: SessionManager
    /** TextView per navigare alla schermata di login */
    private lateinit var tvLoginLink: TextView

    /**
     * Metodo chiamato alla creazione dell'activity.
     * Inizializza le viste e configura i listener per i controlli UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inizializza il gestore della sessione
        session = SessionManager(this)

        // Inizializzazione delle viste
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)

        // Gestione del click sul pulsante di registrazione
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validazione dei campi
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Effettua la registrazione
            register(name, email, password)
        }

        // Gestione del click sul testo per il login
        tvLoginLink.setOnClickListener {
            finish() // Torna alla schermata di login
        }
    }

    /**
     * Effettua la registrazione dell'utente tramite chiamata API.
     * Gestisce la risposta del server, salva i dati di sessione in caso di successo
     * e mostra un dialog per email già registrate.
     * 
     * @param name Nome dell'utente
     * @param email Email dell'utente
     * @param password Password dell'utente
     */
    private fun register(name: String, email: String, password: String) {
        // Prepara i dati per la richiesta
        val registerData = mapOf(
            "name" to name,
            "email" to email,
            "password" to password
        )

        // Effettua la chiamata API
        RetrofitClient.api().register(registerData).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    // Aggiungo log per debug
                    android.util.Log.d("RegisterActivity", "Risposta: $responseBody")
                    
                    if (responseBody != null) {
                        // Controllo sia "token" che "access_token"
                        val token = when {
                            responseBody.containsKey("access_token") -> responseBody["access_token"] as? String
                            responseBody.containsKey("token") -> responseBody["token"] as? String
                            else -> null
                        }
                        
                        // Controllo user_id
                        val userId = when (val id = responseBody["user_id"]) {
                            is Double -> id.toInt()
                            is Int -> id
                            else -> -1
                        }
                        
                        if (token != null && userId != -1) {
                            // Salva il token e l'id utente nella sessione
                            session.saveToken(token)
                            session.saveUserId(userId)
                            
                            // Salva anche il nome utente se disponibile
                            val name = responseBody["name"] as? String ?: etName.text.toString().trim()
                            session.saveUserName(name)
                            
                            // Reindirizza alla schermata principale
                            startActivity(Intent(this@RegisterActivity, ListActivity::class.java))
                            finish()
                        } else {
                            android.util.Log.e("RegisterActivity", "Token o user_id non trovati nella risposta")
                            Toast.makeText(this@RegisterActivity, "Errore durante la registrazione: dati mancanti", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.util.Log.e("RegisterActivity", "Risposta vuota dal server")
                        Toast.makeText(this@RegisterActivity, "Errore durante la registrazione: risposta vuota", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("RegisterActivity", "Errore: $errorBody")
                        
                        // Controlla se l'errore è dovuto a email già registrata (codice 409)
                        if (response.code() == 409) {
                            // Mostra un dialog che suggerisce di fare login
                            androidx.appcompat.app.AlertDialog.Builder(this@RegisterActivity)
                                .setTitle("Email già registrata")
                                .setMessage("Questa email è già registrata. Vuoi accedere con questa email?")
                                .setPositiveButton("Vai al login") { _, _ ->
                                    // Passa l'email alla schermata di login
                                    val intent = Intent()
                                    intent.putExtra("email", etEmail.text.toString().trim())
                                    setResult(RESULT_OK, intent)
                                    finish()
                                }
                                .setNegativeButton("Annulla", null)
                                .show()
                        } else {
                            Toast.makeText(this@RegisterActivity, "Dati non validi", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("RegisterActivity", "Errore durante la lettura dell'errore: ${e.message}")
                        Toast.makeText(this@RegisterActivity, "Errore durante la registrazione", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            /**
             * Gestisce gli errori di connessione durante la chiamata API.
             */
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Errore di connessione", Toast.LENGTH_SHORT).show()
            }
        })
    }
}