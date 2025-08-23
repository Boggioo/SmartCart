package com.example.smartcart.ui.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.data.network.RetrofitClient
import com.example.smartcart.ui.list.CreatedListActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyListsActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var adapter: ListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_lists)
        
        // Abilita la freccia indietro nella barra dell'app
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        session = SessionManager(this)
        
        // Inizializza le viste
        recyclerView = findViewById(R.id.rvLists)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        
        // Configura la RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListAdapter(mutableListOf(), 
            onItemClick = { listId ->
                // Quando si clicca su una lista, apri la CreatedListActivity
                val intent = Intent(this, CreatedListActivity::class.java)
                intent.putExtra("list_id", listId)
                startActivity(intent)
            },
            onDeleteClick = { listId, position ->
                deleteList(listId, position)
            }
        )
        recyclerView.adapter = adapter
        
        // Carica le liste
        loadLists()
    }
    
    private fun loadLists() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        
        val token = "Bearer ${session.getToken()}"
        
        RetrofitClient.api().getLists(token).enqueue(object : Callback<List<Map<String, Any>>> {
            override fun onResponse(
                call: Call<List<Map<String, Any>>>,
                response: Response<List<Map<String, Any>>>
            ) {
                progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body() != null) {
                    val lists = response.body()!!
                    if (lists.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(lists)
                    }
                } else {
                    tvEmpty.text = "Errore nel caricamento delle liste"
                    tvEmpty.visibility = View.VISIBLE
                }
            }
            
            override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                progressBar.visibility = View.GONE
                tvEmpty.text = "Errore di rete: ${t.message}"
                tvEmpty.visibility = View.VISIBLE
            }
        })
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Gestisce il click sulla freccia indietro
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun deleteList(listId: Int, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Elimina lista")
            .setMessage("Sei sicuro di voler eliminare questa lista?")
            .setPositiveButton("Elimina") { _, _ ->
                progressBar.visibility = View.VISIBLE
                
                val token = "Bearer ${session.getToken()}"
                RetrofitClient.api().deleteList(token, listId).enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                        progressBar.visibility = View.GONE
                        
                        if (response.isSuccessful) {
                            adapter.removeItem(position)
                            Toast.makeText(this@MyListsActivity, "Lista eliminata con successo", Toast.LENGTH_SHORT).show()
                            
                            // Se non ci sono più liste, mostra il messaggio vuoto
                            if (adapter.itemCount == 0) {
                                recyclerView.visibility = View.GONE
                                tvEmpty.visibility = View.VISIBLE
                            }
                        } else {
                            Toast.makeText(this@MyListsActivity, "Errore nell'eliminazione della lista", Toast.LENGTH_SHORT).show()
                        }
                    }
                    
                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@MyListsActivity, "Errore di rete: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}