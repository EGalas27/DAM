package com.example.proyectomovil.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectomovil.adaptadores.ClienteAdapter
import com.example.proyectomovil.databinding.ActivityListarClientesBinding
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.entidades.Cliente
import com.google.android.material.snackbar.Snackbar

class ListarClientes : AppCompatActivity() {

    private lateinit var binding: ActivityListarClientesBinding
    private lateinit var adapter: ClienteAdapter
    private lateinit var listaClientes: MutableList<Cliente>
    private lateinit var db: AppDatabase
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListarClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.obtenerInstancia(this)
        listaClientes = db.clienteDAO().listar().toMutableList()

        adapter = ClienteAdapter(listaClientes) { cliente ->
            mostrarDialogo(cliente)
        }

        binding.recyclerClientes.layoutManager = LinearLayoutManager(this)
        binding.recyclerClientes.adapter = adapter

        // Buscador
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val resultado = db.clienteDAO().buscarPorNombre(newText.orEmpty())
                adapter.actualizarLista(resultado)
                return true
            }
        })

        // Lanza el formulario y actualiza si fue exitoso
        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                listaClientes = db.clienteDAO().listar().toMutableList()
                adapter.actualizarLista(listaClientes)
            }
        }

        binding.btnNuevoCliente.setOnClickListener {
            val intent = Intent(this, RegistrarCliente::class.java)
            launcher.launch(intent)
        }
    }

    private fun mostrarDialogo(cliente: Cliente) {
        val opciones = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle("¿Qué deseas hacer con ${cliente.nombre}?")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, RegistrarCliente::class.java).apply {
                            putExtra("modo", "editar")
                            putExtra("id", cliente.id)
                        }
                        launcher.launch(intent)
                    }
                    1 -> confirmarEliminar(cliente)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminar(cliente: Cliente) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Cliente")
            .setMessage("¿Estás seguro de eliminar a ${cliente.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                db.clienteDAO().eliminar(cliente)
                mostrarSnackbar("Cliente eliminado", "#F44336")
                listaClientes.remove(cliente)
                adapter.actualizarLista(listaClientes)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun mostrarSnackbar(mensaje: String, colorHex: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor(colorHex))
            .setTextColor(Color.WHITE)
            .show()
    }
}
