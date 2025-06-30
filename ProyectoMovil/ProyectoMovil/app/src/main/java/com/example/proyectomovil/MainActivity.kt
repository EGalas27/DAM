package com.example.proyectomovil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectomovil.ui.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Aplicar padding para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperar nombre y username desde el intent
        val nombre = intent.getStringExtra("nombre") ?: "Técnico"
        val username = intent.getStringExtra("username") ?: ""
        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        tvBienvenida.text = "Bienvenido, $nombre - $username"

        // Tarjetas del menú
        val cardClientes = findViewById<LinearLayout>(R.id.cardClientes)
        val cardEquipos = findViewById<LinearLayout>(R.id.cardEquipos)
        val cardServicios = findViewById<LinearLayout>(R.id.cardServicios)
        val cardComprobantes = findViewById<LinearLayout>(R.id.cardComprobantes)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        // Animación al hacer clic
        val anim = AnimationUtils.loadAnimation(this, R.anim.scale_up)

        // Listeners con animación y navegación
        cardClientes.setOnClickListener {
            it.startAnimation(anim)
            startActivity(Intent(this, ListarClientes::class.java))
        }

        cardEquipos.setOnClickListener {
            it.startAnimation(anim)
            startActivity(Intent(this, ListarEquipos::class.java))
        }

        cardServicios.setOnClickListener {
            it.startAnimation(anim)
            startActivity(Intent(this, ListarServicios::class.java))
        }

        cardComprobantes.setOnClickListener {
            it.startAnimation(anim)
            startActivity(Intent(this, ListarComprobantes::class.java))
        }

        btnCerrarSesion.setOnClickListener {
            it.startAnimation(anim)
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        // Navega al Login y elimina actividades anteriores
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
