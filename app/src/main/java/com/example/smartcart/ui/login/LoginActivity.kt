package com.example.smartcart.ui.login

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
import com.example.smartcart.ui.register.RegisterActivity
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

        // DEBUG: Cancella i dati di sessione per forzare il login durante lo sviluppo
        // session.clear()

        if (session.isLoggedIn()) {
            // Verifica se il token è ancora valido
            validateToken()
        } else {
            initViews()
        }
    }

    private fun validateToken() {
        val token = "Bearer ${session.getToken()}"
        RetrofitClient.api().validateToken(token).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    // Token valido, vai alla lista
                    startActivity(Intent(this@LoginActivity, ListActivity::class.java))
                    finish()
                } else {
                    // Token non valido, mostra login
                    session.clear()
                    initViews()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                // Errore di rete, mostra login
                session.clear()
                initViews()
                Toast.makeText(this@LoginActivity, "Errore di rete", Toast.LENGTH_SHORT).show()
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