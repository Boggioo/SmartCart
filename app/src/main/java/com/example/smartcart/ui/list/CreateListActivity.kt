package com.example.smartcart.ui.list

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.model.Item

class CreatedListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ItemAdapter(mutableListOf(), object : ItemAdapter.ItemListener {
            override fun onToggleChecked(item: Item, position: Int) {
                Toast.makeText(
                    this@CreatedListActivity,
                    "${item.name} ${if (item.checked) "deselezionato" else "selezionato"}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDelete(item: Item, position: Int) {
                adapter.removeAt(position)
                Toast.makeText(
                    this@CreatedListActivity,
                    "${item.name} rimosso",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        recyclerView.adapter = adapter
    }
}