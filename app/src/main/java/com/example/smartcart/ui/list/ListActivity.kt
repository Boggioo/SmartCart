package com.example.smartcart.ui.list

import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.model.Item

class ListActivity : AppCompatActivity() {

    private lateinit var recyclerItems: RecyclerView
    private lateinit var recyclerSupermarkets: RecyclerView
    private lateinit var btnRefresh: Button
    private lateinit var btnNearby: Button
    private lateinit var editNewItem: EditText

    private lateinit var itemAdapter: ItemAdapter
    private lateinit var supermarketAdapter: SupermarketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        recyclerItems = findViewById(R.id.recyclerItems)
        recyclerSupermarkets = findViewById(R.id.recyclerSupermarkets)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnNearby = findViewById(R.id.btnNearby)
        editNewItem = findViewById(R.id.editNewItem)

        itemAdapter = ItemAdapter(mutableListOf(), object : ItemAdapter.ItemListener {
            override fun onToggleChecked(item: Item, position: Int) { }
            override fun onDelete(item: Item, position: Int) { }
        })
        recyclerItems.layoutManager = LinearLayoutManager(this)
        recyclerItems.adapter = itemAdapter

        supermarketAdapter = SupermarketAdapter(mutableListOf())
        recyclerSupermarkets.layoutManager = LinearLayoutManager(this)
        recyclerSupermarkets.adapter = supermarketAdapter
    }
}
