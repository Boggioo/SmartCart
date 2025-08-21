package com.example.smartcart.data.network

import com.example.smartcart.data.model.Item
import com.example.smartcart.data.model.ItemRequest
import com.example.smartcart.data.model.Supermarket
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("/auth/login")
    fun login(@Body body: Map<String, String>): Call<Map<String, Any>>

    @POST("/auth/register")
    fun register(@Body body: Map<String, String>): Call<Map<String, Any>>

    // Aggiunto endpoint per validare il token
    @GET("/auth/validate")
    fun validateToken(@Header("Authorization") token: String): Call<Map<String, Any>>
    
    // Endpoint per ottenere i supermercati vicini
    @GET("/supermarkets/nearby")
    fun getNearbySupermarkets(
        @Header("Authorization") token: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double
    ): Call<List<Supermarket>>

    @GET("/lists")
    fun getLists(@Header("Authorization") token: String): Call<List<Map<String, Any>>>

    @POST("/lists")
    fun createList(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Call<Map<String, Any>>
    
    @POST("/lists")
    fun createListWithBody(
        @Header("Authorization") token: String,
        @Body body: okhttp3.RequestBody
    ): Call<Map<String, Any>>

    @GET("/items")
    fun getItems(
        @Header("Authorization") token: String,
        @Query("list_id") listId: Int
    ): Call<List<Item>>

    @POST("/items")
    fun addItem(
        @Header("Authorization") token: String,
        @Body item: ItemRequest
    ): Call<Item>

    @PUT("items/{id}")
    fun updateItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body item: Map<String, Boolean>
    ): Call<Item>

    @DELETE("/items/{id}")
    fun deleteItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Map<String, Any>>
    
    @DELETE("lists/{id}")
    fun deleteList(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Map<String, Any>>
    
    @POST("/lists/share")
    fun shareList(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Call<Map<String, Any>>
    
    @GET("/lists/shared")
    fun getSharedLists(
        @Header("Authorization") token: String
    ): Call<List<Map<String, Any>>>

}