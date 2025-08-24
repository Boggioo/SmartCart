package com.example.smartcart.ui.list

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.data.model.Item
import com.example.smartcart.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask

/**
 * Activity per la gestione di una lista della spesa specifica.
 * Permette di visualizzare, aggiungere, modificare ed eliminare elementi dalla lista,
 * gestisce la condivisione della lista con altri utenti e fornisce aggiornamenti automatici.
 */
class CreatedListActivity : AppCompatActivity() {
    
    /**
     * Gestisce la selezione degli elementi del menu.
     * Implementa la navigazione indietro tramite il pulsante home della ActionBar.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /** RecyclerView per gli elementi da comprare */
    private lateinit var recyclerView: RecyclerView
    /** RecyclerView per gli elementi completati */
    private lateinit var recyclerViewCompleted: RecyclerView
    /** TextView per l'intestazione degli elementi completati */
    private lateinit var tvCompletedHeader: TextView
    /** Adapter per gli elementi da comprare */
    private lateinit var adapter: ItemAdapter
    /** Adapter per gli elementi completati */
    private lateinit var completedAdapter: ItemAdapter
    /** Campo di input per il nome dell'elemento */
    private lateinit var etItemName: EditText
    /** Campo di input per la quantità dell'elemento */
    private lateinit var etQuantity: EditText
    /** Pulsante per aggiungere un nuovo elemento */
    private lateinit var btnAddItem: Button
    /** Pulsante per condividere la lista */
    private lateinit var btnShareList: Button
    /** Layout per il refresh tramite swipe */
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    /** Gestore della sessione utente */
    private lateinit var session: SessionManager
    /** ID della lista corrente */
    private var listId: Int = -1
    /** Flag che indica se la lista è condivisa */
    private var isShared: Boolean = false
    /** Timer per l'aggiornamento automatico */
    private var refreshTimer: Timer? = null
    /** Handler per l'esecuzione di operazioni sul thread principale */
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Metodo chiamato alla creazione dell'activity.
     * Inizializza le viste, configura gli adapter e avvia l'aggiornamento automatico.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_created_list)
        
        // Abilita il pulsante indietro nella ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        session = SessionManager(this)
        listId = intent.getIntExtra("listId", intent.getIntExtra("list_id", -1))
        isShared = intent.getBooleanExtra("isShared", false)
        
        if (listId == -1) {
            Toast.makeText(this, "Lista non valida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inizializza le viste
        recyclerView = findViewById(R.id.recyclerView)
        recyclerViewCompleted = findViewById(R.id.recyclerViewCompleted)
        tvCompletedHeader = findViewById(R.id.tvCompletedHeader)
        
        // Configura la RecyclerView per gli elementi da comprare
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(mutableListOf(), object : ItemAdapter.ItemListener {
            override fun onToggleChecked(item: Item, position: Int) {
                toggleItemChecked(item, position)
            }

            override fun onDelete(item: Item, position: Int) {
                deleteItem(item, position)
            }
        }, false)
        recyclerView.adapter = adapter
        
        // Configura la RecyclerView per gli elementi completati
        recyclerViewCompleted.layoutManager = LinearLayoutManager(this)
        completedAdapter = ItemAdapter(mutableListOf(), object : ItemAdapter.ItemListener {
            override fun onToggleChecked(item: Item, position: Int) {
                toggleItemChecked(item, position)
            }

            override fun onDelete(item: Item, position: Int) {
                deleteItem(item, position)
            }
        }, true)
        recyclerViewCompleted.adapter = completedAdapter

        etItemName = findViewById(R.id.etItemName)
        etQuantity = findViewById(R.id.etQuantity)
        btnAddItem = findViewById(R.id.btnAddItem)
        btnShareList = findViewById(R.id.btnShareList)

        btnAddItem.setOnClickListener { onAddItemClicked() }
        btnShareList.setOnClickListener { showShareDialog() }
        
        // Nascondi il pulsante di condivisione se la lista è già condivisa con l'utente
        if (isShared) {
            btnShareList.visibility = android.view.View.GONE
        }
        
        // Configura SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            loadItems()
        }
        
        // Avvia il timer di aggiornamento automatico (ogni 30 secondi)
        startAutoRefresh()
        
        loadItems()
    }

    /** Genera l'header di autorizzazione per le chiamate API */
    private fun authHeader(): String = "Bearer ${session.getToken()}"

    /**
     * Carica gli elementi della lista dal server.
     * Separa gli elementi completati da quelli da completare e aggiorna le RecyclerView.
     */
    private fun loadItems() {
        // Utilizzo OkHttp per disabilitare la cache e forzare un aggiornamento dal server
        RetrofitClient.api().getItems(authHeader(), listId).enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                // Ferma l'indicatore di aggiornamento
                swipeRefreshLayout.isRefreshing = false
                
                if (response.isSuccessful && response.body() != null) {
                    val items = response.body()!!
                    val todoItems = items.filter { !it.checked }.toMutableList()
                    val completedItems = items.filter { it.checked }.toMutableList()
                    
                    adapter.updateData(todoItems)
                    completedAdapter.updateData(completedItems)
                    
                    // Mostra/nascondi l'intestazione degli elementi completati
                    tvCompletedHeader.visibility = if (completedItems.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
                } else {
                    Toast.makeText(this@CreatedListActivity, "Errore caricamento lista (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                // Ferma l'indicatore di aggiornamento
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this@CreatedListActivity, "Errore di rete: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    /**
     * Avvia l'aggiornamento automatico della lista ogni 5 secondi.
     * Garantisce la sincronizzazione in tempo reale con altri utenti.
     */
    private fun startAutoRefresh() {
        // Cancella eventuali timer esistenti
        stopAutoRefresh()
        
        // Crea un nuovo timer che si attiva ogni 5 secondi per garantire una sincronizzazione più frequente
        refreshTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        // Esegui l'aggiornamento solo se l'attività è ancora attiva
                        if (!isFinishing) {
                            loadItems()
                        }
                    }
                }
            }, 0, 5000) // 5 secondi per una sincronizzazione più rapida
        }
    }
    
    /**
     * Ferma l'aggiornamento automatico della lista.
     */
    private fun stopAutoRefresh() {
        refreshTimer?.cancel()
        refreshTimer = null
    }
    
    /**
     * Riavvia l'aggiornamento automatico quando l'activity torna in primo piano.
     */
    override fun onResume() {
        super.onResume()
        // Riavvia il timer quando l'attività torna in primo piano
        startAutoRefresh()
    }
    
    /**
     * Ferma l'aggiornamento automatico quando l'activity va in background.
     */
    override fun onPause() {
        super.onPause()
        // Ferma il timer quando l'attività va in background
        stopAutoRefresh()
    }

    /**
     * Gestisce l'aggiunta di un nuovo elemento alla lista.
     * Valida l'input dell'utente e invia la richiesta al server.
     */
    private fun onAddItemClicked() {
        val name = etItemName.text.toString().trim()
        val qtyText = etQuantity.text.toString().trim()
        val quantity = qtyText.toIntOrNull() ?: 1

        if (name.isEmpty()) {
            Toast.makeText(this, "Inserisci un nome", Toast.LENGTH_SHORT).show()
            return
        }

        val itemRequest = com.example.smartcart.data.model.ItemRequest(name, quantity, listId)
        RetrofitClient.api().addItem(authHeader(), itemRequest)
            .enqueue(object : Callback<Item> {
                override fun onResponse(call: Call<Item>, response: Response<Item>) {
                    if (response.isSuccessful && response.body() != null) {
                        adapter.addItem(response.body()!!)
                        recyclerView.scrollToPosition(0)
                        etItemName.setText("")
                        etQuantity.setText("1")
                        
                        // Forza un aggiornamento completo per sincronizzare con altri utenti
                        loadItems()
                    } else {
                        Toast.makeText(this@CreatedListActivity, "Errore aggiunta (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Item>, t: Throwable) {
                    Toast.makeText(this@CreatedListActivity, "Errore di rete: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * Cambia lo stato di completamento di un elemento.
     * Sposta l'elemento tra le liste "da fare" e "completati".
     * 
     * @param item Elemento da modificare
     * @param position Posizione dell'elemento nella lista
     */
    private fun toggleItemChecked(item: Item, position: Int) {
        val newChecked = !item.checked
        val body = mapOf("checked" to newChecked)
        RetrofitClient.api().updateItem(authHeader(), item.id, body)
            .enqueue(object : Callback<Item> {
                override fun onResponse(call: Call<Item>, response: Response<Item>) {
                    if (response.isSuccessful && response.body() != null) {
                        val updated = response.body()!!
                        
                        // Rimuovi l'elemento dalla lista corrente
                        if (item.checked) {
                            completedAdapter.removeAt(position)
                            adapter.addItem(updated)
                        } else {
                            adapter.removeAt(position)
                            completedAdapter.addItem(updated)
                        }
                        
                        // Aggiorna la visibilità dell'intestazione degli elementi completati
                        tvCompletedHeader.visibility = if (completedAdapter.itemCount == 0) android.view.View.GONE else android.view.View.VISIBLE
                        
                        // Forza un aggiornamento completo per sincronizzare con altri utenti
                        loadItems()
                    } else {
                        Toast.makeText(this@CreatedListActivity, "Errore aggiornamento (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Item>, t: Throwable) {
                    Toast.makeText(this@CreatedListActivity, "Errore di rete: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * Elimina un elemento dalla lista.
     * Rimuove l'elemento sia localmente che dal server.
     * 
     * @param item Elemento da eliminare
     * @param position Posizione dell'elemento nella lista
     */
    private fun deleteItem(item: Item, position: Int) {
        RetrofitClient.api().deleteItem(authHeader(), item.id)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    if (response.isSuccessful) {
                        if (item.checked) {
                            completedAdapter.removeAt(position)
                            // Aggiorna la visibilità dell'intestazione degli elementi completati
                            tvCompletedHeader.visibility = if (completedAdapter.itemCount == 0) android.view.View.GONE else android.view.View.VISIBLE
                        } else {
                            adapter.removeAt(position)
                        }
                        Toast.makeText(this@CreatedListActivity, "${item.name} rimosso", Toast.LENGTH_SHORT).show()
                        
                        // Forza un aggiornamento completo per sincronizzare con altri utenti
                        loadItems()
                    } else {
                        Toast.makeText(this@CreatedListActivity, "Errore eliminazione (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(this@CreatedListActivity, "Errore di rete: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    
    /**
     * Mostra un dialog per la condivisione della lista.
     * Permette all'utente di inserire l'email del destinatario.
     */
    private fun showShareDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_share_list, null)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("Condividi lista")
            .setMessage("Inserisci l'email dell'utente con cui vuoi condividere questa lista")
            .setView(dialogView)
            .setPositiveButton("Condividi") { _, _ ->
                val email = etEmail.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this, "Inserisci un'email valida", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                shareList(email)
            }
            .setNegativeButton("Annulla", null)
            .create()
            
        dialog.show()
    }
    
    /**
     * Condivide la lista con un utente specificato tramite email.
     * Invia la richiesta di condivisione al server.
     * 
     * @param email Email dell'utente con cui condividere la lista
     */
    private fun shareList(email: String) {
        val body = mapOf(
            "list_id" to listId.toString(),
            "email" to email
        )
        
        RetrofitClient.api().shareList(authHeader(), body).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreatedListActivity, "Lista condivisa con successo", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Errore sconosciuto"
                    Toast.makeText(this@CreatedListActivity, "Errore: $errorBody", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@CreatedListActivity, "Errore di rete: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}