package com.example.proyectomovil.entidades

import androidx.room.Embedded
import androidx.room.Relation

data class ComprobanteConCliente(
    @Embedded val comprobante: Comprobante,
    @Relation(
        parentColumn = "clienteId",
        entityColumn = "id"
    )
    val cliente: Cliente?
)