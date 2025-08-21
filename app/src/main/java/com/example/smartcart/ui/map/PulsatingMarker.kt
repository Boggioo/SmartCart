package com.example.smartcart.ui.map

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.animation.AccelerateDecelerateInterpolator
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Marker personalizzato con effetto di pulsazione per evidenziare i supermercati sulla mappa
 */
class PulsatingMarker(private val mapView: MapView) : Marker(mapView) {
    
    private var animator: ValueAnimator? = null
    private var scale = 1.0f
    private var isPulsating = false
    
    /**
     * Avvia l'effetto di pulsazione del marker
     */
    fun startPulsing() {
        if (isPulsating) return
        
        isPulsating = true
        animator = ValueAnimator.ofFloat(1.0f, 1.3f, 1.0f).apply {
            duration = 1000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                scale = animation.animatedValue as Float
                mapView.invalidate()
            }
            start()
        }
    }
    
    /**
     * Ferma l'effetto di pulsazione del marker
     */
    fun stopPulsing() {
        isPulsating = false
        animator?.cancel()
        animator = null
        scale = 1.0f
        mapView.invalidate()
    }
    
    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) {
            return
        }
        
        // Salva lo stato corrente del canvas
        canvas.save()
        
        // Applica la scala per l'effetto di pulsazione
        if (isPulsating && icon != null) {
            val centerX = mPositionPixels.x.toFloat()
            val centerY = mPositionPixels.y.toFloat()
            
            canvas.scale(scale, scale, centerX, centerY)
        }
        
        // Disegna il marker con la scala applicata
        super.draw(canvas, mapView, shadow)
        
        // Ripristina lo stato del canvas
        canvas.restore()
    }
    
    override fun onDetach(mapView: MapView?) {
        stopPulsing()
        super.onDetach(mapView)
    }
}