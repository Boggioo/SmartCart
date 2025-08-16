package com.example.smartcart.data.model

data class Item(
    val id: Int,
    val user_id: Int,
    val name: String,
    val quantity: Int,
    val checked: Boolean,
    val created_at: String,
    val price: Double? = null
) {
    constructor(
        id: Int,
        user_id: Int,
        name: String,
        quantity: Int,
        checked: Boolean,
        created_at: String
    ) : this(id, user_id, name, quantity, checked, created_at, null)
}