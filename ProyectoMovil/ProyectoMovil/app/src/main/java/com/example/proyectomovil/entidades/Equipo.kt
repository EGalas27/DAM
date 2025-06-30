package com.example.proyectomovil.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipo")
data class Equipo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val marca: String,
    val modelo: String,
    val clienteId: Int, // clave foránea referencIA a Cliente
    val descripcionProblema: String,
    val imagen: String
)