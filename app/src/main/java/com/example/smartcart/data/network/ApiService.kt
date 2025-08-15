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

    @GET("/items")
    fun getItems(@Header("Authorization") token: String): Call<List<Item>>

    @POST("/items")
    fun addItem(
        @Header("Authorization") token: String,
        @Body item: ItemRequest
    ): Call<Item>

    @PUT("/items/{id}")
    fun updateItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body item: Map<String, Any>
    ): Call<Item>

    @DELETE("/items/{id}")
    fun deleteItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Map<String, Any>>

    @GET("/supermarkets/nearby")
    fun getNearbySupermarkets(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Call<List<Supermarket>>
}
