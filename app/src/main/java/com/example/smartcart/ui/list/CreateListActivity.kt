package com.example.smartcart.ui.list

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.model.Item
import com.example.smartcart.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreatedListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var tvTotalItems: TextView
    private lateinit var tvTotalPrice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_created_list)

        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inizializza l'adapter con una lista vuota
        adapter = ItemAdapter(mutableListOf(), object : ItemAdapter.ItemListener {
            override fun onToggleChecked(item: Item, position: Int) {
                updateTotals()
            }

            override fun onDelete(item: Item, position: Int) {
                adapter.removeAt(position)
                updateTotals()
            }
        })
        recyclerView.adapter = adapter

        loadItems()
    }

    private fun loadItems() {
        val service = ApiService.create(this)
        service.getItems().enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    response.body()?.let { items ->
                        adapter.updateData(items.toMutableList())
                        updateTotals()
                    }
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                // Gestisci errore
            }
        })
    }

    private fun updateTotals() {
        val totalItems = adapter.itemCount
        tvTotalItems.text = getString(R.string.total_items, totalItems)

        var totalPrice = 0.0
        for (i in 0 until adapter.itemCount) {
            val item = adapter.getItemAtPosition(i)
            totalPrice += (item.price ?: 0.0) * item.quantity
        }
        tvTotalPrice.text = getString(R.string.total_price, totalPrice)
    }
}