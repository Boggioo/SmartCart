package com.example.smartcart.ui.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.ui.auth.LoginActivity
import com.example.smartcart.ui.profile.MyListsActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    
    // Gestione del pulsante indietro nella ActionBar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        // Abilita il pulsante indietro nella ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        session = SessionManager(this)
        findViewById<TextView>(R.id.tvUserName).text = session.getUserName() ?: "Utente"

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        findViewById<Button>(R.id.btnSharedLists).setOnClickListener {
            startActivity(Intent(this, MyListsActivity::class.java))
        }
    }
}