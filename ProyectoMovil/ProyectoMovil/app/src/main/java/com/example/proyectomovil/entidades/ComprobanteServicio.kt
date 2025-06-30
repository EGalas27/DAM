package com.example.proyectomovil.entidades

import androidx.room.Entity


@Entity(primaryKeys = ["comprobanteId", "servicioId"])
data class ComprobanteServicio(
    val comprobanteId: Int,
    val servicioId: Int
)