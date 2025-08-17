package com.example.smartcart.ui.list

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcart.R
import com.example.smartcart.ui.profile.ProfileActivity

class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        // Setup click listeners per i bottoni
        setupButtons()
    }

    private fun setupButtons() {
        // Bottone "Crea nuova lista"
        findViewById<Button>(R.id.btnCreateList).setOnClickListener {
            val intent = Intent(this, CreatedListActivity::class.java)
            startActivity(intent)
        }

        // Bottone "Trova supermercati vicini"
        findViewById<Button>(R.id.btnNearby).setOnClickListener {
            // Per ora mostriamo un messaggio, implementeremo la funzionalità dopo
            android.widget.Toast.makeText(this, "Funzionalità supermercati in sviluppo", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Bottone "Profilo utente"
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}