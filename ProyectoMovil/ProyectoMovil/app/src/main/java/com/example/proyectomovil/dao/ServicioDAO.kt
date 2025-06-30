package com.example.proyectomovil.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.proyectomovil.entidades.Servicio
import com.example.proyectomovil.entidades.ServicioConDetalles

@Dao
interface ServicioDAO {
    @Insert
     fun insertar(servicio: Servicio): Long

    @Update
     fun actualizar(servicio: Servicio)

    @Delete
     fun eliminar(servicio: Servicio)

    @Query("SELECT * FROM servicio ORDER BY fecha DESC")
     fun obtenerTodos(): List<Servicio>

    @Query("SELECT * FROM servicio WHERE id = :id")
     fun obtenerPorId(id: Int): Servicio?

    @Query("SELECT * FROM servicio WHERE estado LIKE :estado")
     fun buscarPorEstado(estado: String): List<Servicio>

    @Query("""
    SELECT s.*, u.nombre AS nombreTecnico, e.marca || ' ' || e.modelo AS nombreEquipo
    FROM servicio s
    INNER JOIN usuario u ON s.usuarioId = u.id
    INNER JOIN equipo e ON s.equipoId = e.id
    """)
    fun listarConDetalles(): List<ServicioConDetalles>

    @Query("DELETE FROM servicio WHERE id = :id")
    fun eliminarPorId(id: Int)

    @Query("SELECT * FROM servicio WHERE equipoId IN (SELECT id FROM equipo WHERE clienteId = :clienteId)")
    fun listarPorCliente(clienteId: Int): List<Servicio>

    @Query("""
        SELECT servicio.* FROM servicio
        INNER JOIN equipo ON servicio.equipoId = equipo.id
        WHERE equipo.clienteId = :clienteId
    """)
    fun listarServiciosPorCliente(clienteId: Int): LiveData<List<Servicio>>

    @Query("""
    SELECT servicio.* FROM servicio
    INNER JOIN equipo ON servicio.equipoId = equipo.id
    WHERE equipo.clienteId = :clienteId""")
    fun getServiciosPorCliente(clienteId: Int): LiveData<List<Servicio>>

}

