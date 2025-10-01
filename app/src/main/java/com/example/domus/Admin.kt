package com.example.domus

import kotlinx.serialization.Serializable

@Serializable
data class Admin(
    val id: Int? = null,
    val usuario: String,
    val senha_hash: String,
    val tipo: String,
    val data_cadastro: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)