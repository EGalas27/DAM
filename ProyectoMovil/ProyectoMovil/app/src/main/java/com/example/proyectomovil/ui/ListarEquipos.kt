package com.example.proyectomovil.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectomovil.adaptadores.EquipoAdapter
import com.example.proyectomovil.databinding.ActivityListarEquiposBinding
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.entidades.Equipo

class ListarEquipos : AppCompatActivity() {

    private lateinit var binding: ActivityListarEquiposBinding
    private lateinit var adapter: EquipoAdapter
    private lateinit var db: AppDatabase
    private var mapaClientes = emptyMap<Int, String>()
    private val listaEquipos = mutableListOf<Equipo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListarEquiposBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.obtenerInstancia(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Cargar datos iniciales
        cargarDatos()

        // Inicializar adaptador
        adapter = EquipoAdapter(listaEquipos, mapaClientes) { equipo ->
            mostrarDialogoOpciones(equipo)
        }

        binding.recyclerEquipos.layoutManager = LinearLayoutManager(this)
        binding.recyclerEquipos.adapter = adapter

        // Botón para ir a registrar nuevo equipo
        binding.btnNuevoEquipo.setOnClickListener {
            val intent = Intent(this, RegistrarEquipo::class.java)
            startActivity(intent)
        }

        // Buscar por cliente
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val texto = newText.orEmpty().trim()
                val resultado = if (texto.isEmpty()) {
                    listaEquipos
                } else {
                    listaEquipos.filter {
                        mapaClientes[it.clienteId]?.contains(texto, ignoreCase = true) == true
                    }
                }
                adapter.actualizarDatos(resultado, mapaClientes)
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
        adapter.actualizarDatos(listaEquipos, mapaClientes)

    }

    private fun cargarDatos() {
        val clientes = db.clienteDAO().listar()
        mapaClientes = clientes.associateBy({ it.id }, { it.nombre })

        listaEquipos.clear()
        listaEquipos.addAll(db.equipoDAO().listar())
    }


    private fun mostrarDialogoOpciones(equipo: Equipo) {
        val opciones = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle("Opciones del equipo")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> editarEquipo(equipo)
                    1 -> confirmarEliminacion(equipo)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun editarEquipo(equipo: Equipo) {
        val intent = Intent(this, RegistrarEquipo::class.java)
        intent.putExtra("equipoId", equipo.id)
        startActivity(intent)
    }

    private fun confirmarEliminacion(equipo: Equipo) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar equipo?")
            .setMessage("¿Deseas eliminar el equipo de marca '${equipo.marca}'?")
            .setPositiveButton("Sí") { _, _ ->
                db.equipoDAO().eliminar(equipo)
                cargarDatos()
                adapter.actualizarDatos(listaEquipos, mapaClientes)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
