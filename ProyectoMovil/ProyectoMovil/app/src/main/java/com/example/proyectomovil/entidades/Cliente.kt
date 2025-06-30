package com.example.proyectomovil.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cliente")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val telefono: String,
    val email: String,
    val direccion: String,
    val numeroDocumento: String
)
