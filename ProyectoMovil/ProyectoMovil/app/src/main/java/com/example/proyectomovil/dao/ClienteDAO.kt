package com.example.proyectomovil.dao

import androidx.room.*
import com.example.proyectomovil.entidades.Cliente

@Dao
interface ClienteDAO {

    @Insert
    fun insertar(cliente: Cliente)

    @Update
    fun actualizar(cliente: Cliente)

    @Delete
    fun eliminar(cliente: Cliente)

    @Query("SELECT * FROM cliente ORDER BY nombre ASC")
    fun listar(): List<Cliente>

    @Query("SELECT * FROM cliente WHERE id = :id LIMIT 1")
    fun buscarPorId(id: Int): Cliente?

    @Query("SELECT * FROM cliente WHERE nombre LIKE '%' || :texto || '%' ORDER BY nombre ASC")
    fun buscarPorNombre(texto: String): List<Cliente>
}
