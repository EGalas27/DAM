package com.example.proyectomovil.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servicio")
data class Servicio(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val equipoId: Int,
    val usuarioId: Int,
    val categoria: String,       // Revisión, Mantenimiento, Reparación
    val descripcion: String,
    val fecha: String,
    val costo: Double,
    val estado: String         //para saber estado del servicio (pendiente, en proceso o finalizado)
)
