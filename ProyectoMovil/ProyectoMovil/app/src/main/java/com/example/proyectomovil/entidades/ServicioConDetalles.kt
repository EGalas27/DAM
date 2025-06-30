package com.example.proyectomovil.entidades

data class ServicioConDetalles(
    val id: Int,
    val fecha: String,
    val equipoId: Int,
    val usuarioId: Int,
    val categoria: String,
    val descripcion: String,
    val costo: Double,
    val estado: String,
    val nombreTecnico: String,
    val nombreEquipo: String
)
