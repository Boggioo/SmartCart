package com.example.smartcart.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.data.network.RetrofitClient
import com.example.smartcart.ui.list.ListActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        session = SessionManager(this)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
            } else if (password != confirm) {
                Toast.makeText(this, "Le password non coincidono", Toast.LENGTH_SHORT).show()
            } else {
                register(name, email, password)
            }
        }
    }

    private fun register(name: String, email: String, password: String) {
        val body = mapOf(
            "name" to name,
            "email" to email,
            "password" to password
        )

        RetrofitClient.api().register(body).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body() ?: return
                    val token = responseBody["access_token"] as? String
                    val userId = (responseBody["user_id"] as? Number)?.toInt() ?: -1

                    if (token != null && userId != -1) {
                        session.saveToken(token)
                        session.saveUserId(userId)
                        session.saveUserName(name)

                        Toast.makeText(this@RegisterActivity, "Registrazione riuscita", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, ListActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Errore nei dati di registrazione", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Email già registrata o errore", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Errore di rete", Toast.LENGTH_SHORT).show()
            }
        })
    }
}