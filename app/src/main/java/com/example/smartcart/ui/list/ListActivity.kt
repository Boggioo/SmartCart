package com.example.smartcart.ui.list

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.data.model.Supermarket
import com.example.smartcart.data.network.RetrofitClient
import com.example.smartcart.ui.profile.ProfileActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListActivity : AppCompatActivity() {

    private lateinit var recyclerSupermarkets: RecyclerView
    private lateinit var btnNearby: Button
    private lateinit var btnCreateList: Button
    private lateinit var btnProfile: Button
    private lateinit var supermarketAdapter: SupermarketAdapter
    private lateinit var session: SessionManager
    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        session = SessionManager(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        recyclerSupermarkets = findViewById(R.id.recyclerSupermarkets)
        btnNearby = findViewById(R.id.btnNearby)
        btnCreateList = findViewById(R.id.btnCreateList)
        btnProfile = findViewById(R.id.btnProfile)

        supermarketAdapter = SupermarketAdapter(mutableListOf())
        recyclerSupermarkets.layoutManager = LinearLayoutManager(this)
        recyclerSupermarkets.adapter = supermarketAdapter

        btnCreateList.setOnClickListener {
            startActivity(Intent(this, CreateListActivity::class.java))
        }

        btnNearby.setOnClickListener {
            checkLocationPermission()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
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