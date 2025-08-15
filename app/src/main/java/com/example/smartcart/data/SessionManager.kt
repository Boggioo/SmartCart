package com.example.smartcart.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(ctx: Context) {
    private val prefs: SharedPreferences =
        ctx.getSharedPreferences("smartcart_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) { prefs.edit().putString("token", token).apply() }
    fun getToken(): String? = prefs.getString("token", null)

    fun saveUserId(id: Int) { prefs.edit().putInt("user_id", id).apply() }
    fun getUserId(): Int = prefs.getInt("user_id", -1)

    fun clear() { prefs.edit().clear().apply() }
}