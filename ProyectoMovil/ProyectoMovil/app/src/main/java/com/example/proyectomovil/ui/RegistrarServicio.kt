package com.example.proyectomovil.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.proyectomovil.databinding.ActivityRegistrarServicioBinding
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.entidades.Servicio
import com.google.android.material.snackbar.Snackbar
import java.util.*

class RegistrarServicio : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarServicioBinding
    private lateinit var db: AppDatabase
    private var mapaEquipos = emptyMap<String, Int>()
    private var mapaUsuarios = emptyMap<String, Int>()
    private var servicioIdEditar: Int = -1 // -1 indica registro nuevo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarServicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.obtenerInstancia(this)
        binding.progressBarServicio.visibility = View.GONE

        // Cargar datos de forma asíncrona
        lifecycleScope.launch {
            cargarDatosIniciales()
        }

        configurarDatePicker()

        binding.btnGuardarServicio.setOnClickListener {
            guardarServicio()
        }

        binding.btnVerServicios.setOnClickListener {
            startActivity(Intent(this, ListarServicios::class.java))
        }
    }

    private suspend fun cargarDatosIniciales() {
        // Mostrar progress bar mientras carga
        binding.progressBarServicio.visibility = View.VISIBLE

        try {
            // Cargar spinners en hilo de fondo
            withContext(Dispatchers.IO) {
                cargarSpinners()
            }

            // Verificar si es edición
            servicioIdEditar = intent.getIntExtra("servicio_id", -1)
            if (servicioIdEditar != -1) {
                cargarServicioParaEditar()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Snackbar.make(binding.root, "Error al cargar datos: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        } finally {
            // Ocultar progress bar
            binding.progressBarServicio.visibility = View.GONE
        }
    }

    private suspend fun cargarServicioParaEditar() {
        try {
            val servicio = withContext(Dispatchers.IO) {
                db.servicioDao().obtenerPorId(servicioIdEditar)
            }

            if (servicio != null) {
                // Actualizar UI en el hilo principal
                withContext(Dispatchers.Main) {
                    cargarDatosEnFormulario(servicio)
                    binding.tvTituloServicio.text = "Editar Servicio"
                    binding.btnGuardarServicio.text = "Actualizar Servicio"
                }
            } else {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Servicio no encontrado", Snackbar.LENGTH_LONG).show()
                    finish()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Snackbar.make(binding.root, "Error al cargar servicio: ${e.message}", Snackbar.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun configurarDatePicker() {
        binding.etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val mes = String.format("%02d", month + 1)
                    val dia = String.format("%02d", day)
                    binding.etFecha.setText("$year-$mes-$dia")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun cargarSpinners() {
        val equipos = db.equipoDAO().listar()
        val usuarios = db.usuarioDAO().listar()

        mapaEquipos = equipos.associateBy({ "${it.marca} ${it.modelo}" }, { it.id })
        mapaUsuarios = usuarios.associateBy({ it.nombre }, { it.id })

        // Actualizar UI en el hilo principal
        runOnUiThread {
            binding.autoEquipo.setAdapter(
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mapaEquipos.keys.toList())
            )
            binding.autoTecnico.setAdapter(
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mapaUsuarios.keys.toList())
            )
        }
    }

    private fun cargarDatosEnFormulario(servicio: Servicio) {
        binding.etFecha.setText(servicio.fecha)

        val equipoNombre = mapaEquipos.entries.firstOrNull { it.value == servicio.equipoId }?.key
        val usuarioNombre = mapaUsuarios.entries.firstOrNull { it.value == servicio.usuarioId }?.key

        binding.autoEquipo.setText(equipoNombre ?: "", false)
        binding.autoTecnico.setText(usuarioNombre ?: "", false)
        binding.etDescripcion.setText(servicio.descripcion)
        binding.etCosto.setText(servicio.costo.toString())

        // Limpiar selecciones previas
        binding.chipRevision.isChecked = false
        binding.chipMantenimiento.isChecked = false
        binding.chipReparacion.isChecked = false

        when (servicio.categoria) {
            "Revisión" -> binding.chipRevision.isChecked = true
            "Mantenimiento" -> binding.chipMantenimiento.isChecked = true
            "Reparación" -> binding.chipReparacion.isChecked = true
        }

        // Para RadioButtons (opción recomendada)
        when (servicio.estado) {
            "Pendiente" -> binding.rbPendiente.isChecked = true
            "En Proceso" -> binding.rbProceso.isChecked = true
            "Finalizado" -> binding.rbFinalizado.isChecked = true
        }
    }

    private fun guardarServicio() {
            val fecha = binding.etFecha.text.toString().trim()
            val equipoNombre = binding.autoEquipo.text.toString().trim()
            val tecnicoNombre = binding.autoTecnico.text.toString().trim()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val costoTexto = binding.etCosto.text.toString().trim()

            val categoria = when {
                binding.chipRevision.isChecked -> "Revisión"
                binding.chipMantenimiento.isChecked -> "Mantenimiento"
                binding.chipReparacion.isChecked -> "Reparación"
                else -> ""
            }

            val estado = when {
                binding.rbPendiente.isChecked -> "Pendiente"
                binding.rbProceso.isChecked -> "En Proceso"
                binding.rbFinalizado.isChecked -> "Finalizado"
                else -> ""
            }

            val errores = mutableListOf<String>()
            if (fecha.isEmpty()) errores.add("Fecha")
            if (equipoNombre.isEmpty()) errores.add("Equipo")
            if (tecnicoNombre.isEmpty()) errores.add("Técnico")
            if (descripcion.isEmpty()) errores.add("Descripción")
            if (costoTexto.isEmpty()) errores.add("Costo")
            if (categoria.isEmpty()) errores.add("Categoría")
            if (estado.isEmpty()) errores.add("Estado")

            if (errores.isNotEmpty()) {
                Snackbar.make(binding.root, "Faltan campos: ${errores.joinToString()}", Snackbar.LENGTH_LONG).show()
                return
            }

            val equipoId = mapaEquipos[equipoNombre]
            val usuarioId = mapaUsuarios[tecnicoNombre]
            val costo = costoTexto.toDoubleOrNull() ?: 0.0

            if (equipoId == null || usuarioId == null) {
                Snackbar.make(binding.root, "Equipo o técnico inválido", Snackbar.LENGTH_LONG).show()
                return
            }

            val servicio = Servicio(
                id = if (servicioIdEditar == -1) 0 else servicioIdEditar,
                fecha = fecha,
                equipoId = equipoId,
                usuarioId = usuarioId,
                categoria = categoria,
                descripcion = descripcion,
                costo = costo,
                estado = estado
            )

            // Mostrar progress
            binding.progressBarServicio.visibility = View.VISIBLE

            // Esperar 4 segundos y luego guardar
            Handler(Looper.getMainLooper()).postDelayed({
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            if (servicioIdEditar == -1) {
                                db.servicioDao().insertar(servicio)
                            } else {
                                db.servicioDao().actualizar(servicio)
                            }
                        }

                        // Ocultar progress y mostrar Snackbar
                        binding.progressBarServicio.visibility = View.GONE
                        val mensaje = if (servicioIdEditar == -1)
                            "✅ Servicio registrado correctamente"
                        else
                            "✅ Servicio actualizado correctamente"

                        Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_INDEFINITE)
                            .setAction("Aceptar") { finish() }
                            .show()

                    } catch (e: Exception) {
                        binding.progressBarServicio.visibility = View.GONE
                        Snackbar.make(binding.root, "Error al guardar: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            }, 4000)
    }
}