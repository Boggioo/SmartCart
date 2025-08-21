package com.example.smartcart.ui.map

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.smartcart.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

/**
 * Finestra informativa personalizzata per i marker dei supermercati
 * Mostra le informazioni in modo permanente sopra il marker
 */
class CustomInfoWindow(
    mapView: MapView,
    private val marker: Marker
) : InfoWindow(R.layout.custom_info_window, mapView) {

    override fun onOpen(item: Any?) {
        // Ottieni i riferimenti alle viste nel layout
        val tvTitle = mView.findViewById<TextView>(R.id.tvTitle)
        val tvSnippet = mView.findViewById<TextView>(R.id.tvSnippet)
        val cardView = mView.findViewById<CardView>(R.id.cardView)
        
        // Imposta il titolo e il contenuto
        tvTitle.text = marker.title
        tvSnippet.text = marker.snippet
        
        // Personalizza l'aspetto
        cardView.setCardBackgroundColor(Color.WHITE)
        
        // Posiziona la finestra sopra il marker
        mView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    }

    override fun onClose() {
        // Non fare nulla per mantenere la finestra sempre aperta
    }
}