package com.example.smartcart.ui.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.ui.login.LoginActivity
import com.example.smartcart.ui.profile.SharedListsActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        session = SessionManager(this)
        findViewById<TextView>(R.id.tvUserName).text = session.getUserName() ?: "Utente"

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        findViewById<Button>(R.id.btnSharedLists).setOnClickListener {
            startActivity(Intent(this, SharedListsActivity::class.java))
        }
    }
}