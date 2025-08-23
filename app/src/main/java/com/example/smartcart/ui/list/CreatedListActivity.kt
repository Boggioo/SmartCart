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

class CreatedListActivity : AppCompatActivity() {
    
    // Gestione del pulsante indietro nella ActionBar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewCompleted: RecyclerView
    private lateinit var tvCompletedHeader: TextView
    private lateinit var adapter: ItemAdapter
    private lateinit var completedAdapter: ItemAdapter
    private lateinit var etItemName: EditText
    private lateinit var etQuantity: EditText
    private lateinit var btnAddItem: Button
    private lateinit var btnShareList: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var session: SessionManager
    private var listId: Int = -1
    private var isShared: Boolean = false
    private var refreshTimer: Timer? = null
    private val handler = Handler(Looper.getMainLooper())

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

    private fun authHeader(): String = "Bearer ${session.getToken()}"

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
    
    private fun stopAutoRefresh() {
        refreshTimer?.cancel()
        refreshTimer = null
    }
    
    override fun onResume() {
        super.onResume()
        // Riavvia il timer quando l'attività torna in primo piano
        startAutoRefresh()
    }
    
    override fun onPause() {
        super.onPause()
        // Ferma il timer quando l'attività va in background
        stopAutoRefresh()
    }

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