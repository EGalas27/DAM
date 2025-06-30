package com.example.proyectomovil.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.proyectomovil.entidades.Comprobante
import com.example.proyectomovil.entidades.ComprobanteConCliente

@Dao
interface ComprobanteDAO {
    @Insert
    suspend fun insertar(comprobante: Comprobante): Long

    @Query("SELECT * FROM comprobante")
    fun listar(): LiveData<List<Comprobante>>

    @Query("SELECT * FROM comprobante WHERE clienteId = :clienteId")
    fun listarPorCliente(clienteId: Int): LiveData<List<Comprobante>>

    @Delete
     fun eliminar(comprobante: Comprobante)

    @Query("SELECT comprobante.*, cliente.nombre as clienteNombre " +
            "FROM comprobante INNER JOIN cliente ON comprobante.clienteId = cliente.id")
     fun listarConClientes(): List<Comprobante>

    @Transaction
    @Query("SELECT * FROM comprobante")
    suspend fun listarConCliente(): List<ComprobanteConCliente>

    @Transaction
    @Query("SELECT * FROM comprobante WHERE clienteId = :clienteId")
    suspend fun listarPorClienteConDetalles(clienteId: Int): List<ComprobanteConCliente>
}


