package com.example.proyectomovil.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectomovil.R
import com.example.proyectomovil.databinding.ActivityRegistrarClienteBinding
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.entidades.Cliente
import com.google.android.material.snackbar.Snackbar

class RegistrarCliente : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarClienteBinding
    private lateinit var db: AppDatabase
    private var modoEdicion = false
    private var idCliente = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegistrarClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.obtenerInstancia(this)

        // Revisamos si estamos en modo edición
        modoEdicion = intent.getStringExtra("modo") == "editar"
        idCliente = intent.getIntExtra("id", -1)

        if (modoEdicion && idCliente != -1) {
            binding.tvTitulo.text = "Actualizar Cliente"
            binding.btnGuardar.text = "Actualizar Cliente"

            val cliente = db.clienteDAO().buscarPorId(idCliente)
            cliente?.let {
                binding.etNombre.setText(it.nombre)
                binding.etTelefono.setText(it.telefono)
                binding.etEmail.setText(it.email)
                binding.etDireccion.setText(it.direccion)
            }
        } else {
            binding.tvTitulo.text = "Registrar Cliente"
            binding.btnGuardar.text = "Guardar Cliente"
        }

        binding.btnGuardar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val direccion = binding.etDireccion.text.toString().trim()
            val numero = binding.etNumeroDocumento.text.toString().trim()

            if (nombre.isEmpty()) {
                mostrarSnackbarSimple("⚠️ El nombre es obligatorio", "#FFA000")
                return@setOnClickListener
            }

            val cliente = Cliente(
                id = if (modoEdicion) idCliente else 0,
                nombre = nombre,
                telefono = telefono,
                email = email,
                direccion = direccion,
                numeroDocumento = numero
            )

            if (modoEdicion) {
                db.clienteDAO().actualizar(cliente)
                mostrarSnackbarConCerrar("✅ Cliente actualizado correctamente", "#4CAF50")
            } else {
                db.clienteDAO().insertar(cliente)
                mostrarSnackbarConCerrar("✅ Cliente registrado correctamente",  "#2196F3")

            }
        }

        // Si prefieres no tener este botón, puedes ocultarlo o eliminarlo.
        binding.btnVerClientes.setOnClickListener {
            val intent = Intent(this, ListarClientes::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun limpiarCampos() {
        binding.etNombre.text?.clear()
        binding.etTelefono.text?.clear()
        binding.etEmail.text?.clear()
        binding.etDireccion.text?.clear()
        binding.etNombre.requestFocus()
        binding.etNumeroDocumento.requestFocus()
    }

    // ✅ Snackbar con botón Aceptar que cierra la vista
    private fun mostrarSnackbarConCerrar(mensaje: String, colorHex: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_INDEFINITE)
            .setBackgroundTint(Color.parseColor(colorHex))
            .setTextColor(Color.WHITE)
            .setAction("Aceptar") {
                setResult(RESULT_OK)
                finish()  // ← Regresa a la lista de clientes
            }
            .setActionTextColor(Color.YELLOW)
            .show()
    }

    // ⚠️ Para errores o alertas simples (como campo vacío)
    private fun mostrarSnackbarSimple(mensaje: String, colorHex: String) {
        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor(colorHex))
            .setTextColor(Color.WHITE)
            .show()
    }
}


