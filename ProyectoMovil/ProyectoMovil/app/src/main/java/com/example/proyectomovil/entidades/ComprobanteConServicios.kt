package com.example.proyectomovil.entidades

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

// (Clase POJO para consultas)
data class ComprobanteConServicios(
    @Embedded val comprobante: Comprobante,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ComprobanteServicio::class,
            parentColumn = "comprobanteId",
            entityColumn = "servicioId"
        )
    )
    val servicios: List<Servicio>
)