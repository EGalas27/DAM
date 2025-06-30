package com.example.proyectomovil.util

import android.content.Context
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.entidades.Usuario

object DbSeeder {
    fun insertarUsuarioAdmin(context: Context) {
        val db = AppDatabase.obtenerInstancia(context)
        val dao = db.usuarioDAO()

        // insertar usuario admin
        val existeAdmin = dao.listar().any { it.username == "admin" }
        if (!existeAdmin) {
            val passAdmin = SeguridadPassword.hashSHA256("admin")
            val usuarioAdmin = Usuario(
                username = "admin",
                password = passAdmin,
                nombre = "Jholby Segura",
                rol = "admin"
            )
            dao.insertar(usuarioAdmin)
        }

        val existeTecnico = dao.listar().any { it.username == "tecnico" }
        if (!existeTecnico) {
            val passTecnico = SeguridadPassword.hashSHA256("tecnico")
            val usuarioTecnico = Usuario(
                username = "tecnico",
                password = passTecnico,
                nombre = "Emanuel Galas",
                rol = "tecnico"
            )
            dao.insertar(usuarioTecnico)
        }
    }
}
