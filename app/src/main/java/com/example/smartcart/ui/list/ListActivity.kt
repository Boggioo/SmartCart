package com.example.smartcart.ui.list

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.data.model.Item
import com.example.smartcart.data.model.ItemRequest
import com.example.smartcart.data.model.Supermarket
import com.example.smartcart.data.network.RetrofitClient
import com.example.smartcart.ui.profile.ProfileActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListActivity : AppCompatActivity() {

    private lateinit var recyclerItems: RecyclerView
    private lateinit var recyclerSupermarkets: RecyclerView
    private lateinit var btnRefresh: Button
    private lateinit var btnNearby: Button
    private lateinit var btnAddItem: Button
    private lateinit var btnProfile: Button
    private lateinit var editNewItem: EditText

    private lateinit var itemAdapter: ItemAdapter
    private lateinit var supermarketAdapter: SupermarketAdapter
    private lateinit var session: SessionManager
    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        session = SessionManager(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        recyclerItems = findViewById(R.id.recyclerItems)
        recyclerSupermarkets = findViewById(R.id.recyclerSupermarkets)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnNearby = findViewById(R.id.btnNearby)
        btnAddItem = findViewById(R.id.btnAddItem)
        btnProfile = findViewById(R.id.btnProfile)
        editNewItem = findViewById(R.id.editNewItem)

        itemAdapter = ItemAdapter(mutableListOf(), object : ItemAdapter.ItemListener {
            override fun onToggleChecked(item: Item, position: Int) {
                updateItemChecked(item, position)
            }
            override fun onDelete(item: Item, position: Int) {
                deleteItem(item, position)
            }
        })
        recyclerItems.layoutManager = LinearLayoutManager(this)
        recyclerItems.adapter = itemAdapter

        supermarketAdapter = SupermarketAdapter(mutableListOf())
        recyclerSupermarkets.layoutManager = LinearLayoutManager(this)
        recyclerSupermarkets.adapter = supermarketAdapter

        // Carica gli articoli all'avvio
        loadItems()

        btnAddItem.setOnClickListener {
            val itemName = editNewItem.text.toString().trim()
            if (itemName.isNotEmpty()) {
                addNewItem(itemName)
            } else {
                Toast.makeText(this, "Inserisci un prodotto", Toast.LENGTH_SHORT).show()
            }
        }

        btnNearby.setOnClickListener {
            checkLocationPermission()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnRefresh.setOnClickListener {
            loadItems()
        }
    }

    private fun loadItems() {
        val token = "Bearer ${session.getToken()}"
        RetrofitClient.api().getItems(token).enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        itemAdapter.setData(it.toMutableList())
                    }
                } else {
                    Toast.makeText(this@ListActivity, "Errore: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                Toast.makeText(this@ListActivity, "Errore: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addNewItem(name: String) {
        val token = "Bearer ${session.getToken()}"
        val itemRequest = ItemRequest(name, 1) // Quantità predefinita 1

        RetrofitClient.api().addItem(token, itemRequest).enqueue(object : Callback<Item> {
            override fun onResponse(call: Call<Item>, response: Response<Item>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        itemAdapter.addOne(it)
                        editNewItem.text.clear()
                    }
                } else {
                    Toast.makeText(this@ListActivity, "Errore: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Item>, t: Throwable) {
                Toast.makeText(this@ListActivity, "Errore: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateItemChecked(item: Item, position: Int) {
        val token = "Bearer ${session.getToken()}"
        val updateData = mapOf("checked" to !item.checked)

        RetrofitClient.api().updateItem(token, item.id, updateData).enqueue(object : Callback<Item> {
            override fun onResponse(call: Call<Item>, response: Response<Item>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        itemAdapter.updateAt(position, it)
                    }
                }
            }
            override fun onFailure(call: Call<Item>, t: Throwable) {
                Toast.makeText(this@ListActivity, "Errore: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteItem(item: Item, position: Int) {
        val token = "Bearer ${session.getToken()}"

        RetrofitClient.api().deleteItem(token, item.id).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    itemAdapter.removeAt(position)
                }
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@ListActivity, "Errore: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        } else {
            Toast.makeText(this, "Permesso negato", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLocation() {
        try {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            location?.let {
                fetchNearbySupermarkets(it.latitude, it.longitude)
            } ?: Toast.makeText(this, "Posizione non disponibile", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun fetchNearbySupermarkets(lat: Double, lon: Double) {
        RetrofitClient.api().getNearbySupermarkets(lat, lon).enqueue(object : Callback<List<Supermarket>> {
            override fun onResponse(call: Call<List<Supermarket>>, response: Response<List<Supermarket>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        supermarketAdapter.setData(it.toMutableList())
                    }
                } else {
                    Toast.makeText(this@ListActivity, "Errore: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Supermarket>>, t: Throwable) {
                Toast.makeText(this@ListActivity, "Errore: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}