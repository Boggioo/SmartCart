package com.example.smartcart.data.network

import com.example.smartcart.data.model.Item
import com.example.smartcart.data.model.ItemRequest
import com.example.smartcart.data.model.Supermarket
import retrofit2.Call
import retrofit2.http.*

/**
 * Interfaccia per le chiamate API REST al backend Flask.
 * Definisce tutti gli endpoint disponibili per l'autenticazione,
 * la gestione delle liste della spesa e dei supermercati.
 */
interface ApiService {

    /** Endpoint per l'autenticazione dell'utente */
    @POST("/auth/login")
    fun login(@Body body: Map<String, String>): Call<Map<String, Any>>

    /** Endpoint per la registrazione di un nuovo utente */
    @POST("/auth/register")
    fun register(@Body body: Map<String, String>): Call<Map<String, Any>>

    /** Endpoint per validare un token JWT esistente */
    @GET("/auth/validate")
    fun validateToken(@Header("Authorization") token: String): Call<Map<String, Any>>
    
    /** 
     * Endpoint per ottenere i supermercati nelle vicinanze di una posizione.
     * @param token Token JWT per l'autenticazione
     * @param latitude Latitudine della posizione
     * @param longitude Longitudine della posizione
     * @param radius Raggio di ricerca in chilometri
     */
    @GET("/supermarkets/nearby")
    fun getNearbySupermarkets(
        @Header("Authorization") token: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double
    ): Call<List<Supermarket>>

    /** Endpoint per ottenere tutte le liste dell'utente autenticato */
    @GET("/lists")
    fun getLists(@Header("Authorization") token: String): Call<List<Map<String, Any>>>

    /** Endpoint per creare una nuova lista della spesa */
    @POST("/lists")
    fun createList(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Call<Map<String, Any>>
    
    /** Endpoint alternativo per creare una lista con RequestBody personalizzato */
    @POST("/lists")
    fun createListWithBody(
        @Header("Authorization") token: String,
        @Body body: okhttp3.RequestBody
    ): Call<Map<String, Any>>

    /** 
     * Endpoint per ottenere tutti gli articoli di una lista specifica.
     * @param token Token JWT per l'autenticazione
     * @param listId ID della lista di cui recuperare gli articoli
     */
    @GET("/items")
    fun getItems(
        @Header("Authorization") token: String,
        @Query("list_id") listId: Int
    ): Call<List<Item>>

    /** 
     * Endpoint per aggiungere un nuovo articolo a una lista.
     * @param token Token JWT per l'autenticazione
     * @param item Dati dell'articolo da aggiungere
     */
    @POST("/items")
    fun addItem(
        @Header("Authorization") token: String,
        @Body item: ItemRequest
    ): Call<Item>

    /** 
     * Endpoint per aggiornare lo stato di un articolo (es. completato/non completato).
     * @param token Token JWT per l'autenticazione
     * @param id ID dell'articolo da aggiornare
     * @param item Mappa con i nuovi valori dell'articolo
     */
    @PUT("items/{id}")
    fun updateItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body item: Map<String, Boolean>
    ): Call<Item>

    /** 
     * Endpoint per eliminare un articolo da una lista.
     * @param token Token JWT per l'autenticazione
     * @param id ID dell'articolo da eliminare
     */
    @DELETE("/items/{id}")
    fun deleteItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Map<String, Any>>
    
    /** 
     * Endpoint per eliminare una lista della spesa.
     * @param token Token JWT per l'autenticazione
     * @param id ID della lista da eliminare
     */
    @DELETE("lists/{id}")
    fun deleteList(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Map<String, Any>>
    
    /** 
     * Endpoint per condividere una lista con altri utenti.
     * @param token Token JWT per l'autenticazione
     * @param body Dati per la condivisione (es. email dell'utente destinatario)
     */
    @POST("/lists/share")
    fun shareList(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Call<Map<String, Any>>
    
    /** 
     * Endpoint per ottenere tutte le liste condivise con l'utente.
     * @param token Token JWT per l'autenticazione
     */
    @GET("/lists/shared")
    fun getSharedLists(
        @Header("Authorization") token: String
    ): Call<List<Map<String, Any>>>

}