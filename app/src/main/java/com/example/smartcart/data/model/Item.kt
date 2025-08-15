package com.example.smartcart.data.model

data class Item(
    val id: Int,
    val user_id: Int,
    val name: String,
    val quantity: Int,
    var checked: Boolean
)
