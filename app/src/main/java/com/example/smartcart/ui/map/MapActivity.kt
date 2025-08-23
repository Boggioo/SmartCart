package com.example.smartcart.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.MenuItem
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartcart.R
import com.example.smartcart.data.SessionManager
import com.example.smartcart.data.model.Supermarket
import com.example.smartcart.data.network.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapActivity : AppCompatActivity() {

    // Gestione del pulsante indietro nella ActionBar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private lateinit var map: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var session: SessionManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    
    // Launcher per la richiesta dei permessi di localizzazione
    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val locationPermissionGranted = permissions.entries.any { it.value }
            
            if (locationPermissionGranted) {
                // Permesso concesso, ottieni la posizione
                getLastLocation()
            } else {
                // Permesso negato, mostra un dialogo per spiegare perché è necessario
                showPermissionDeniedDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurazione di OSMDroid
        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = packageName
        
        setContentView(R.layout.activity_map)
        
        // Abilita il pulsante indietro nella ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inizializza il gestore della sessione
        session = SessionManager(this)

        // Inizializza il client per la posizione
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Inizializza la mappa
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(14.0)
        
        // Richiedi i permessi di localizzazione usando il nuovo sistema
        checkAndRequestLocationPermissions()
    }
    
    private fun checkAndRequestLocationPermissions() {
        when {
            // Verifica se abbiamo già i permessi
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Abbiamo già i permessi, ottieni la posizione
                getLastLocation()
            }
            
            // Verifica se dobbiamo mostrare una spiegazione
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Mostra una spiegazione all'utente
                AlertDialog.Builder(this)
                    .setTitle("Permesso di localizzazione necessario")
                    .setMessage("Per mostrare i supermercati vicini, l'app ha bisogno di accedere alla tua posizione.")
                    .setPositiveButton("OK") { _, _ ->
                        // Richiedi i permessi
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                    .setNegativeButton("Annulla") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(
                            this,
                            "Impossibile mostrare i supermercati vicini senza permessi di localizzazione",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .create()
                    .show()
            }
            
            // Altrimenti, richiedi direttamente i permessi
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permesso negato")
            .setMessage("Senza il permesso di localizzazione, non possiamo mostrarti i supermercati vicini. Vuoi abilitare il permesso nelle impostazioni?")
            .setPositiveButton("Impostazioni") { _, _ ->
                // Apri le impostazioni dell'app
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("No, grazie") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Impossibile mostrare i supermercati vicini senza permessi di localizzazione",
                    Toast.LENGTH_LONG
                ).show()
            }
            .create()
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        map.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        // Verifica se dobbiamo mostrare la spiegazione prima di richiedere il permesso
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Mostra una spiegazione all'utente *in modo asincrono*
            Toast.makeText(
                this,
                "La localizzazione è necessaria per mostrare i supermercati vicini",
                Toast.LENGTH_LONG
            ).show()
        }
        
        // Richiedi sia ACCESS_FINE_LOCATION che ACCESS_COARSE_LOCATION
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Controlla se almeno uno dei permessi è stato concesso
            val locationPermissionGranted = grantResults.isNotEmpty() && 
                (grantResults[0] == PackageManager.PERMISSION_GRANTED || 
                (grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))
            
            if (locationPermissionGranted) {
                // Permesso concesso, ottieni la posizione
                getLastLocation()
            } else {
                Toast.makeText(
                    this,
                    "Permesso di localizzazione negato. Impossibile mostrare i supermercati vicini.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getLastLocation() {
        // Verifica se abbiamo i permessi necessari
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Se non abbiamo i permessi, richiediamoli di nuovo
            requestLocationPermission()
            return
        }

        // Mostra un messaggio all'utente
        Toast.makeText(this, "Ricerca della tua posizione in corso...", Toast.LENGTH_SHORT).show()

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentGeoPoint = GeoPoint(it.latitude, it.longitude)
                    map.controller.setCenter(currentGeoPoint)
                    
                    // Aggiungi un marker per la posizione attuale
                    val marker = Marker(map)
                    marker.position = currentGeoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "La tua posizione"
                    
                    // Personalizza il marker della posizione utente
                    val userIcon = ContextCompat.getDrawable(this, R.drawable.ic_my_location)
                    userIcon?.setTint(Color.BLUE) // Colora l'icona in blu
                    marker.icon = userIcon
                    
                    // Applica l'animazione di rimbalzo al marker della posizione
                    applyBounceAnimation(marker)
                    
                    map.overlays.add(marker)
                    
                    fetchNearbySupermarkets(it.latitude, it.longitude)
                } ?: run {
                    Toast.makeText(
                        this,
                        "Impossibile ottenere la posizione attuale. Assicurati che la localizzazione sia attiva nelle impostazioni del dispositivo.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Errore durante l'accesso alla posizione: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
    
    /**
     * Applica un'animazione di rimbalzo a un marker
     */
    private fun applyBounceAnimation(marker: Marker) {
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        marker.infoWindow?.view?.startAnimation(bounceAnimation)
    }

    private fun fetchNearbySupermarkets(latitude: Double, longitude: Double) {
        val token = "Bearer ${session.getToken()}"

        // Verifica che il token sia valido
        if (session.getToken().isNullOrEmpty()) {
            Toast.makeText(
                this,
                "Sessione scaduta, effettua nuovamente il login",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        RetrofitClient.api().getNearbySupermarkets(
            token,
            latitude,
            longitude,
            5.0 // Raggio di 5 km
        ).enqueue(object : Callback<List<Supermarket>> {
            override fun onResponse(
                call: Call<List<Supermarket>>,
                response: Response<List<Supermarket>>
            ) {
                if (response.isSuccessful) {
                    val supermarkets = response.body()
                    supermarkets?.let {
                        displaySupermarkets(it)
                    }
                } else {
                    Toast.makeText(
                        this@MapActivity,
                        "Errore nel caricamento dei supermercati: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Supermarket>>, t: Throwable) {
                Toast.makeText(
                    this@MapActivity,
                    "Errore di rete: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Formatta la distanza in un formato più leggibile
     */
    private fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} metri"
            else -> String.format("%.1f km", distanceKm)
        }
    }
    
    // Variabile per tenere traccia del marker attualmente selezionato
    private var selectedMarker: PulsatingMarker? = null
    
    private fun displaySupermarkets(supermarkets: List<Supermarket>) {
        if (supermarkets.isEmpty()) {
            Toast.makeText(
                this,
                "Nessun supermercato trovato nelle vicinanze",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // Rimuovi i marker esistenti (tranne quello della posizione attuale)
        val positionMarker = map.overlays.firstOrNull()
        map.overlays.clear()
        positionMarker?.let { map.overlays.add(it) }
        
        // Ordina i supermercati per distanza
        val sortedSupermarkets = supermarkets.sortedBy { it.distance }
        
        // Lista di tutti i marker dei supermercati
        val supermarketMarkers = mutableListOf<PulsatingMarker>()
        
        for (supermarket in sortedSupermarkets) {
            val position = GeoPoint(supermarket.latitude, supermarket.longitude)
            val marker = PulsatingMarker(map)
            marker.position = position
            marker.title = supermarket.name
            
            // Formatta la distanza in modo più leggibile
            val formattedDistance = formatDistance(supermarket.distance)
            marker.snippet = "${supermarket.address}\nDistanza: ${formattedDistance}"
            
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.icon = ContextCompat.getDrawable(this, R.drawable.ic_supermarket)
            
            // Configura l'InfoWindow personalizzata
            marker.setInfoWindow(CustomInfoWindow(map, marker))
            
            // Aggiungi un listener per il click sul marker
            marker.setOnMarkerClickListener { clickedMarker, _ ->
                // Disattiva la pulsazione del marker precedentemente selezionato
                selectedMarker?.stopPulsing()
                
                // Chiudi tutte le InfoWindow attualmente aperte
                for (overlay in map.overlays) {
                    if (overlay is Marker && overlay != clickedMarker) {
                        overlay.closeInfoWindow()
                    }
                }
                
                // Attiva la pulsazione del marker corrente
                (clickedMarker as? PulsatingMarker)?.let {
                    it.startPulsing()
                    selectedMarker = it
                }
                
                // Centra la mappa sul supermercato selezionato e zooma leggermente
                map.controller.animateTo(clickedMarker.position)
                map.controller.setZoom(16.0) // Zoom più vicino quando si seleziona un supermercato
                
                // Mostra l'InfoWindow solo quando si clicca sul marker
                clickedMarker.showInfoWindow()
                
                true // Indica che l'evento è stato gestito
            }
            
            supermarketMarkers.add(marker)
            map.overlays.add(marker)
        }
        
        // Fai pulsare il marker del supermercato più vicino
        supermarketMarkers.firstOrNull()?.let {
            it.startPulsing()
            selectedMarker = it
        }
        
        // Aggiorna la mappa
        map.invalidate()
        
        // Mostra un breve messaggio con il numero di supermercati trovati
        Toast.makeText(
            this,
            "Trovati ${supermarkets.size} supermercati nelle vicinanze",
            Toast.LENGTH_SHORT
        ).show()
    }
}