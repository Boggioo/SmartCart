package com.example.smartcart.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.data.local.AppDatabase
import com.example.smartcart.data.local.UserEntity
import com.example.smartcart.data.network.RetrofitClient
import com.example.smartcart.ui.list.ListActivity
import com.example.smartcart.ui.register.RegisterActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        session = SessionManager(this)
        
        // Cancella i dati di sessione per richiedere sempre il login
        session.clear()
        
        // Inizializza le viste per il login manuale
        initViews()
    }

    private fun attemptAutoLoginFromDb() {
        lifecycleScope.launch {
            try {
                val saved = withContext(Dispatchers.IO) {
                    AppDatabase.get(this@LoginActivity).userDao().getCurrentUser()
                }
                if (saved != null && !saved.token.isNullOrEmpty()) {
                    // Log per debug
                    android.util.Log.d("LoginActivity", "Utente trovato nel DB: ${saved.name}, token: ${saved.token}")
                    
                    session.saveUserId(saved.id)
                    session.saveToken(saved.token)
                    session.saveUserName(saved.name)
                    validateToken()
                } else {
                    android.util.Log.d("LoginActivity", "Nessun utente trovato nel DB o token non valido")
                    initViews()
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Errore durante il recupero dell'utente dal DB: ${e.message}", e)
                initViews()
            }
        }
    }

    private fun validateToken() {
        val token = "Bearer ${session.getToken()}"
        android.util.Log.d("LoginActivity", "Validazione token: $token")
        
        RetrofitClient.api().validateToken(token).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    // Token valido, vai alla lista
                    android.util.Log.d("LoginActivity", "Token valido, accesso alla lista")
                    startActivity(Intent(this@LoginActivity, ListActivity::class.java))
                    finish()
                } else {
                    // Token non valido, mostra login
                    android.util.Log.d("LoginActivity", "Token non valido (${response.code()}), mostra login")
                    // Rimuovi l'utente dal database locale
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            AppDatabase.get(this@LoginActivity).userDao().clear()
                            android.util.Log.d("LoginActivity", "Utente rimosso dal DB")
                        } catch (e: Exception) {
                            android.util.Log.e("LoginActivity", "Errore durante la rimozione dell'utente dal DB: ${e.message}", e)
                        }
                    }
                    session.clear()
                    initViews()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                // Errore di rete, mostra login ma non cancellare le credenziali
                // In caso di errore di rete, potremmo voler mantenere le credenziali per un nuovo tentativo
                android.util.Log.e("LoginActivity", "Errore di rete durante la validazione del token: ${t.message}", t)
                initViews()
                Toast.makeText(this@LoginActivity, "Errore di rete: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoRegister = findViewById(R.id.tvGoRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                Toast.makeText(this, "Email e password richiesti", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login(email: String, password: String) {
        val body = mapOf("email" to email, "password" to password)
        RetrofitClient.api().login(body).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val map = response.body()!!

                    // Gestione tipo di dato per user_id
                    val userId = when (val id = map["user_id"]) {
                        is Double -> id.toInt()
                        is Int -> id
                        else -> -1
                    }

                    val token = map["access_token"] as? String

                    if (token != null && userId != -1) {
                        session.saveToken(token)
                        session.saveUserId(userId)
                        // Persist user locally for auto-login
                        val name = map["name"] as? String ?: "Utente"
                        session.saveUserName(name) // Salva il nome utente nella SessionManager
                        val emailSafe = etEmail.text.toString().trim()
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                AppDatabase.get(this@LoginActivity).userDao()
                                    .upsert(UserEntity(id = userId, name = name, email = emailSafe, token = token))
                                android.util.Log.d("LoginActivity", "Utente salvato nel DB: $name, token: $token")
                            } catch (e: Exception) {
                                android.util.Log.e("LoginActivity", "Errore durante il salvataggio dell'utente nel DB: ${e.message}", e)
                            }
                        }

                        Toast.makeText(this@LoginActivity, "Login riuscito", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, ListActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Dati di login non validi", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Credenziali non valide"
                        400 -> "Richiesta non valida"
                        else -> "Errore sconosciuto: ${response.code()}"
                    }
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    "Errore di rete: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}