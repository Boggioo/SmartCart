package com.example.smartcart.data.model

data class Item(
    val id: Int,
    val name: String,
    val quantity: Int,
    val checked: Boolean = false
)