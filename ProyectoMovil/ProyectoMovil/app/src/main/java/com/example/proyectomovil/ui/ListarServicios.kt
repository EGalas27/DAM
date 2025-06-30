package com.example.proyectomovil.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectomovil.adaptadores.ServicioAdapter
import com.example.proyectomovil.databinding.ActivityListarServiciosBinding
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.entidades.ServicioConDetalles

class ListarServicios : AppCompatActivity() {

    private lateinit var binding: ActivityListarServiciosBinding
    private lateinit var db: AppDatabase
    private lateinit var servicioAdapter: ServicioAdapter
    private var listaServicios = mutableListOf<ServicioConDetalles>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListarServiciosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        db = AppDatabase.obtenerInstancia(this)

        // Obtener lista inicial
        listaServicios = db.servicioDao().listarConDetalles().toMutableList()

        // Configurar RecyclerView
        servicioAdapter = ServicioAdapter(this, listaServicios) {
            // Esta función se ejecuta después de eliminar un servicio
            actualizarLista()
        }

        binding.recyclerServicios.apply {
            layoutManager = LinearLayoutManager(this@ListarServicios)
            adapter = servicioAdapter
        }

        // Configurar búsqueda
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarServicios(newText.orEmpty())
                return true
            }
        })
        binding.btnNuevoServicio.setOnClickListener {
            val intent = Intent(this, RegistrarServicio::class.java)
            startActivity(intent)
        }
    }

    // Filtrar servicios por texto
    private fun filtrarServicios(texto: String) {
        val filtrados = listaServicios.filter {
            it.descripcion.contains(texto, ignoreCase = true) ||
                    it.categoria.contains(texto, ignoreCase = true) ||
                    it.estado.contains(texto, ignoreCase = true) ||
                    it.nombreTecnico.contains(texto, ignoreCase = true) ||
                    it.nombreEquipo.contains(texto, ignoreCase = true)
        }
        servicioAdapter.actualizarLista(filtrados)
    }

    // Actualizar lista completa desde la base de datos
    private fun actualizarLista() {
        listaServicios = db.servicioDao().listarConDetalles().toMutableList()
        servicioAdapter.actualizarLista(listaServicios)
    }

    override fun onResume() {
        super.onResume()
        actualizarLista()
    }
}
