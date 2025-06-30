package com.example.proyectomovil.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectomovil.R
import com.example.proyectomovil.databinding.ActivityRegistrarEquipoBinding
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.entidades.Cliente
import com.example.proyectomovil.entidades.Equipo
import com.google.android.material.snackbar.Snackbar

class RegistrarEquipo : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarEquipoBinding
    private lateinit var galeriaLauncher: ActivityResultLauncher<Intent>
    private var uriImagenSeleccionada: Uri? = null
    private val PERMISO_GALERIA = 101
    private lateinit var db: AppDatabase
    private lateinit var listaClientes: List<Cliente>
    private var equipoExistente: Equipo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistrarEquipoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.obtenerInstancia(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Cargar clientes
        listaClientes = db.clienteDAO().listar()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaClientes.map { it.nombre })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerClientes.adapter = adapter

        // Verificar si es edición
        val equipoId = intent.getIntExtra("equipoId", -1)
        if (equipoId != -1) {
            equipoExistente = db.equipoDAO().buscarPorId(equipoId)
            equipoExistente?.let { equipo ->
                binding.etMarca.setText(equipo.marca)
                binding.etModelo.setText(equipo.modelo)
                binding.etProblema.setText(equipo.descripcionProblema)
                uriImagenSeleccionada = equipo.imagen.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }
                uriImagenSeleccionada?.let { binding.ivEquipo.setImageURI(it) }
                val pos = listaClientes.indexOfFirst { it.id == equipo.clienteId }
                if (pos >= 0) binding.spinnerClientes.setSelection(pos)
                binding.tvTituloEquipo.text = "Actualizar Equipo"
                binding.btnGuardarEquipo.text = "Actualizar"
            }
        }

        // Seleccionar imagen
        galeriaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                uriImagenSeleccionada = data?.data
                binding.ivEquipo.setImageURI(uriImagenSeleccionada)
            }
        }

        binding.btnSeleccionarImagen.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISO_GALERIA
                )
            } else {
                abrirGaleria()
            }
        }

        // Botón guardar o actualizar
        binding.btnGuardarEquipo.setOnClickListener {
            val marca = binding.etMarca.text.toString().trim()
            val modelo = binding.etModelo.text.toString().trim()
            val descripcion = binding.etProblema.text.toString().trim()
            val posicion = binding.spinnerClientes.selectedItemPosition
            val clienteSeleccionado = if (posicion in listaClientes.indices) listaClientes[posicion] else null
            val imagen = uriImagenSeleccionada?.toString() ?: ""

            if (marca.isEmpty() || modelo.isEmpty() || descripcion.isEmpty() || clienteSeleccionado == null) {
                Snackbar.make(binding.root, "⚠️ Todos los campos son obligatorios, excepto la imagen", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Aceptar") {}.show()
                return@setOnClickListener
            }

            if (equipoExistente == null) {
                val nuevoEquipo = Equipo(
                    id = 0,
                    marca = marca,
                    modelo = modelo,
                    clienteId = clienteSeleccionado.id,
                    descripcionProblema = descripcion,
                    imagen = imagen
                )
                val idGenerado = db.equipoDAO().insertar(nuevoEquipo)
                if (idGenerado > 0) {
                    Snackbar.make(binding.root, "✅ Equipo registrado correctamente", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Aceptar") { finish() }
                        .show()
                } else {
                    Snackbar.make(binding.root, "❌ Error al registrar el equipo", Snackbar.LENGTH_LONG).show()
                }
            } else {
                val actualizado = equipoExistente!!.copy(
                    marca = marca,
                    modelo = modelo,
                    clienteId = clienteSeleccionado.id,
                    descripcionProblema = descripcion,
                    imagen = imagen
                )
                db.equipoDAO().actualizar(actualizado)
                Snackbar.make(binding.root, "✅ Equipo actualizado correctamente", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Aceptar") { finish() }
                    .show()
            }
        }

        binding.btnVerLista.setOnClickListener {
            startActivity(Intent(this, ListarEquipos::class.java))
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        galeriaLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISO_GALERIA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria()
        } else {
            Snackbar.make(binding.root, "Permiso denegado para acceder a imágenes", Snackbar.LENGTH_LONG).show()
        }
    }
}
