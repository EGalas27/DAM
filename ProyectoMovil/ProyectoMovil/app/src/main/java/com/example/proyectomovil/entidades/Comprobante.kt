package com.example.proyectomovil.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class Comprobante(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: String,
    val clienteId: Int,
    val fechaEmision: String,
    val subtotal: Double,
    val igv: Double,
    val total: Double,
    val totalDolares: Double
)