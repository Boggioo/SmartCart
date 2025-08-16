package com.example.smartcart.ui.list

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.data.model.Item
import com.example.smartcart.data.model.ItemRequest
import com.example.smartcart.data.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateListActivity : AppCompatActivity() {

    private lateinit var recyclerItems: RecyclerView
    private lateinit var btnAddItem: Button
    private lateinit var editNewItem: EditText
    private lateinit var btnSaveList: Button
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_list)

        session = SessionManager(this)

        recyclerItems = findViewById(R.id.recyclerItems)
        btnAddItem = findViewById(R.id.btnAddItem)
        editNewItem = findViewById(R.id.editNewItem)
        btnSaveList = findViewById(R.id.btnSaveList)

        itemAdapter = ItemAdapter(mutableListOf(), object : ItemAdapter.ItemListener {
            override fun onToggleChecked(item: Item, position: Int) {}
            override fun onDelete(item: Item, position: Int) {
                itemAdapter.removeAt(position)
            }
        })
        recyclerItems.layoutManager = LinearLayoutManager(this)
        recyclerItems.adapter = itemAdapter

        btnAddItem.setOnClickListener {
            val itemName = editNewItem.text.toString().trim()
            if (itemName.isNotEmpty()) {
                val newItem = Item(
                    id = -1,
                    user_id = session.getUserId(),
                    name = itemName,
                    quantity = 1,
                    checked = false
                )
                itemAdapter.addOne(newItem)
                editNewItem.text.clear()
            } else {
                Toast.makeText(this, "Inserisci un prodotto", Toast.LENGTH_SHORT).show()
            }
        }

        btnSaveList.setOnClickListener {
            saveListToServer()
        }
    }

    private fun saveListToServer() {
        if (itemAdapter.itemCount == 0) {
            Toast.makeText(this, "La lista è vuota", Toast.LENGTH_SHORT).show()
            return
        }

        val token = "Bearer ${session.getToken()}"
        val items = itemAdapter.data.filter { it.name.isNotEmpty() }

        var successCount = 0
        val totalItems = items.size

        items.forEach { item ->
            val itemRequest = ItemRequest(item.name, item.quantity)
            RetrofitClient.api().addItem(token, itemRequest).enqueue(object : Callback<Item> {
                override fun onResponse(call: Call<Item>, response: Response<Item>) {
                    if (response.isSuccessful) {
                        successCount++
                    } else {
                        Toast.makeText(this@CreateListActivity, "Errore salvataggio: ${item.name}", Toast.LENGTH_SHORT).show()
                    }

                    if (successCount == totalItems) {
                        Toast.makeText(this@CreateListActivity, "Lista salvata con successo!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                override fun onFailure(call: Call<Item>, t: Throwable) {
                    Toast.makeText(this@CreateListActivity, "Errore rete: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}