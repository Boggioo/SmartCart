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

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoRegister: TextView
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inizializza il gestore della sessione
        session = SessionManager(this)
        
        // Cancella la sessione per richiedere sempre il login
        session.clear()
        
        // Non controlliamo più se l'utente è già loggato
        // in modo da richiedere sempre l'autenticazione

        // Inizializzazione delle viste
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoRegister = findViewById(R.id.tvGoRegister)

        // Gestione del click sul pulsante di login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validazione dei campi
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Inserisci email e password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Effettua il login
            login(email, password)
        }

        // Gestione del click sul testo per la registrazione
        tvGoRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivityForResult(intent, REGISTER_REQUEST_CODE)
        }
    }
    
    // Costante per il codice di richiesta
    companion object {
        private const val REGISTER_REQUEST_CODE = 100
    }
    
    // Gestione del risultato dell'attività di registrazione
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REGISTER_REQUEST_CODE && resultCode == RESULT_OK) {
            // Ottieni l'email passata dalla schermata di registrazione
            val email = data?.getStringExtra("email")
            email?.let {
                // Compila automaticamente il campo email
                etEmail.setText(it)
                // Metti il focus sul campo password
                etPassword.requestFocus()
                // Mostra un messaggio all'utente
                Toast.makeText(this, "Inserisci la password per accedere", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(email: String, password: String) {
        // Prepara i dati per la richiesta
        val loginData = mapOf("email" to email, "password" to password)

        // Mostra un messaggio di caricamento
        val progressDialog = android.app.ProgressDialog(this)
        progressDialog.setMessage("Login in corso...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        // Effettua la chiamata API
        RetrofitClient.api().login(loginData).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                progressDialog.dismiss()
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    android.util.Log.d("LoginActivity", "Risposta: $responseBody")
                    
                    if (responseBody != null) {
                        try {
                            // Verifica se c'è un token nella risposta
                            if (responseBody.containsKey("access_token")) {
                                // Salva il token nella sessione
                                val token = responseBody["access_token"] as String
                                session.saveToken(token)
                                
                                // Salva anche l'ID utente se presente
                                if (responseBody.containsKey("user_id")) {
                                    val userId = (responseBody["user_id"] as Double).toInt()
                                    session.saveUserId(userId)
                                }
                                
                                // Salva anche il nome utente se presente
                                if (responseBody.containsKey("name")) {
                                    val name = responseBody["name"] as String
                                    session.saveUserName(name)
                                }

                                // Reindirizza alla schermata principale
                                startActivity(Intent(this@LoginActivity, ListActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Token non trovato nella risposta", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("LoginActivity", "Errore nel parsing della risposta: ${e.message}")
                            Toast.makeText(this@LoginActivity, "Errore nel parsing della risposta", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Risposta vuota dal server", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("LoginActivity", "Errore: $errorBody")
                        Toast.makeText(this@LoginActivity, "Credenziali non valide", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Errore durante il login", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                progressDialog.dismiss()
                android.util.Log.e("LoginActivity", "Errore di connessione: ${t.message}")
                Toast.makeText(this@LoginActivity, "Errore di connessione", Toast.LENGTH_SHORT).show()
            }
        })
    }
}