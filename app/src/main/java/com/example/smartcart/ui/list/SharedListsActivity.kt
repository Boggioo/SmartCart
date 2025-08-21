package com.example.smartcart.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SharedListsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoSharedLists: TextView
    private lateinit var session: SessionManager
    private lateinit var adapter: SharedListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_lists)

        session = SessionManager(this)
        
        // Inizializza le viste
        recyclerView = findViewById(R.id.rvLists)
        tvNoSharedLists = findViewById(R.id.tvEmpty)
        
        // Configura la RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SharedListAdapter { listId ->
            // Quando si clicca su una lista, apri la lista
            val intent = Intent(this, CreatedListActivity::class.java)
            intent.putExtra("listId", listId)
            intent.putExtra("isShared", true)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        
        // Carica le liste condivise
        loadSharedLists()
    }
    
    private fun authHeader(): String = "Bearer ${session.getToken()}"
    
    private fun loadSharedLists() {
        RetrofitClient.api().getSharedLists(authHeader()).enqueue(object : Callback<List<Map<String, Any>>> {
            override fun onResponse(call: Call<List<Map<String, Any>>>, response: Response<List<Map<String, Any>>>) {
                if (response.isSuccessful && response.body() != null) {
                    val sharedLists = response.body()!!
                    if (sharedLists.isEmpty()) {
                        tvNoSharedLists.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvNoSharedLists.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateData(sharedLists)
                    }
                } else {
                    Toast.makeText(this@SharedListsActivity, "Errore caricamento liste condivise (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                Toast.makeText(this@SharedListsActivity, "Errore di rete: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}