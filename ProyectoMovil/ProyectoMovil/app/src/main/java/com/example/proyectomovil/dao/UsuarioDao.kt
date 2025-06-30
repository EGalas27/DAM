package com.example.proyectomovil.dao

import androidx.room.*
import com.example.proyectomovil.entidades.Usuario

@Dao
interface UsuarioDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuario WHERE username = :usuario AND password = :password LIMIT 1")
    fun login(usuario: String, password: String): Usuario?

    @Query("SELECT * FROM usuario")
    fun listar(): List<Usuario>

    @Query("SELECT * FROM usuario WHERE id = :id")
    fun obtenerPorId(id: Int): Usuario?

    @Query("SELECT * FROM usuario WHERE username = :username LIMIT 1")
    fun buscarPorUsername(username: String): Usuario?
}
