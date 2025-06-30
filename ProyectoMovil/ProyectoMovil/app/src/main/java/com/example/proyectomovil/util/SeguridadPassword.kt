package com.example.proyectomovil.util

import java.security.MessageDigest  // para hashear clave

object SeguridadPassword {
    fun hashSHA256(texto: String): String {
        val bytes = texto.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
