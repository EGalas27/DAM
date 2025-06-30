package com.example.proyectomovil.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.proyectomovil.entidades.Equipo

@Dao
interface EquipoDAO {

    @Insert
    fun insertar(equipo: Equipo): Long

    @Update
    fun actualizar(equipo: Equipo)

    @Delete
    fun eliminar(equipo: Equipo)

    @Query("SELECT * FROM equipo ORDER BY id DESC")
    fun listar(): List<Equipo>

    @Query("SELECT * FROM equipo WHERE id = :id")
    fun buscarPorId(id: Int): Equipo?

    @Query("SELECT * FROM equipo WHERE clienteId = :clienteId")
    fun buscarPorCliente(clienteId: Int): List<Equipo>
}