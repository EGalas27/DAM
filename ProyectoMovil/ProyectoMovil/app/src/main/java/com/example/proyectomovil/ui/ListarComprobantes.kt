package com.example.proyectomovil.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectomovil.R
import com.example.proyectomovil.adaptadores.ComprobanteAdapter
import com.example.proyectomovil.databinding.ActivityListarComprobantesBinding
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.entidades.Cliente
import com.example.proyectomovil.entidades.Comprobante
import com.example.proyectomovil.entidades.ComprobanteConCliente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListarComprobantes : AppCompatActivity() {

    private lateinit var binding: ActivityListarComprobantesBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: ComprobanteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityListarComprobantesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = AppDatabase.obtenerInstancia(this)

        configurarRecyclerView()
        configurarBusqueda()
        cargarComprobantes()


        binding.btnNuevoComprobante.setOnClickListener {
            val intent = Intent(this, RegistrarComprobante::class.java)
            startActivity(intent)

        }
    }

    private fun configurarRecyclerView() {
        adapter = ComprobanteAdapter(
            context = this,
            onEliminarClick = { comprobante -> mostrarDialogoEliminar(comprobante) },
            onDetallesClick = { comprobante -> mostrarDetallesComprobante(comprobante) }
        )

        binding.rvComprobantes.apply {
            layoutManager = LinearLayoutManager(this@ListarComprobantes)
            adapter = this@ListarComprobantes.adapter
            setHasFixedSize(true)
        }
    }

    private fun configurarBusqueda() {
        binding.etBuscar.addTextChangedListener { text ->
            adapter.filtrarPorCliente(text.toString())
        }
    }


    private fun cargarComprobantes() {
        lifecycleScope.launch {
            try {
                val comprobantes = withContext(Dispatchers.IO) {
                    database.comprobanteDAO().listarConCliente()
                }

                if (comprobantes.isEmpty()) {
                    binding.tvSinComprobantes.visibility = View.VISIBLE
                    binding.rvComprobantes.visibility = View.GONE
                } else {
                    binding.tvSinComprobantes.visibility = View.GONE
                    binding.rvComprobantes.visibility = View.VISIBLE
                    adapter.actualizarLista(comprobantes)  // Nombre correcto del método
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ListarComprobantes,  // Contexto correcto
                    "Error al cargar comprobantes: ${e.message}",  // Parámetro correcto
                    Toast.LENGTH_LONG  // Constante correcta
                ).show()
            }
        }
    }

    private fun mostrarDialogoEliminar(comprobante: Comprobante) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar comprobante")
            .setMessage("¿Está seguro de eliminar este comprobante? Esta acción no se puede deshacer")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarComprobante(comprobante)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarComprobante(comprobante: Comprobante) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    database.comprobanteServicioDAO().eliminarPorComprobante(comprobante.id)
                    database.comprobanteDAO().eliminar(comprobante)
                }
                adapter.removerComprobante(comprobante)
                mostrarMensaje("Comprobante eliminado")
            } catch (e: Exception) {
                mostrarMensaje("Error al eliminar: ${e.message}")
            }
        }
    }

    private fun mostrarDetallesComprobante(comprobante: Comprobante) {
        lifecycleScope.launch {
            try {
                val detalles = withContext(Dispatchers.IO) {
                    val servicios = database.comprobanteServicioDAO()
                        .obtenerServiciosPorComprobante(comprobante.id)
                    val cliente = database.clienteDAO()
                        .buscarPorId(comprobante.clienteId)

                    Triple(servicios, cliente, comprobante)
                }

                val (servicios, cliente, comprobante) = detalles
                val mensaje = buildString {
                    append("Cliente: ${cliente?.nombre ?: "No encontrado"}\n")
                    append("Tipo: ${comprobante.tipo}\n")
                    append("Fecha: ${comprobante.fechaEmision}\n\n")
                    append("Servicios incluidos:\n")
                    servicios.forEach {
                        append("• ${it.descripcion} - S/ ${it.costo}\n")
                    }
                    append("\nTotal: S/ ${"%.2f".format(comprobante.total)}")
                    if (comprobante.tipo == "Factura") {
                        append("\nSubtotal: S/ ${"%.2f".format(comprobante.subtotal)}")
                        append("\nIGV (18%): S/ ${"%.2f".format(comprobante.igv)}")
                    }
                }

                AlertDialog.Builder(this@ListarComprobantes)
                    .setTitle("Detalles del comprobante")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar", null)
                    .show()
            } catch (e: Exception) {
                mostrarMensaje("Error al cargar detalles")
            }
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        cargarComprobantes()
    }

}