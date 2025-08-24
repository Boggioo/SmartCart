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

/**
 * Activity che gestisce il profilo utente.
 * Mostra le informazioni dell'utente e fornisce opzioni per logout e accesso alle liste.
 */
class ProfileActivity : AppCompatActivity() {

    /** Gestore della sessione utente */
    private lateinit var session: SessionManager
    
    /**
     * Gestisce la selezione degli elementi del menu, incluso il pulsante indietro.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Inizializza l'activity del profilo configurando l'interfaccia utente e i listener.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        // Abilita il pulsante indietro nella ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inizializza il gestore della sessione e mostra il nome utente
        session = SessionManager(this)
        findViewById<TextView>(R.id.tvUserName).text = session.getUserName() ?: "Utente"

        // Configura il pulsante di logout
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        // Configura il pulsante per accedere alle liste condivise
        findViewById<Button>(R.id.btnSharedLists).setOnClickListener {
            startActivity(Intent(this, MyListsActivity::class.java))
        }
    }
}