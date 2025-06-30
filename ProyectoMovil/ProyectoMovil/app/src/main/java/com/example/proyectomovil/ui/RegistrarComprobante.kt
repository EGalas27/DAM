package com.example.proyectomovil.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.proyectomovil.R
import com.example.proyectomovil.databinding.ActivityRegistrarComprobanteBinding
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.entidades.Comprobante
import com.example.proyectomovil.entidades.ComprobanteServicio
import com.example.proyectomovil.entidades.Servicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrarComprobante : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarComprobanteBinding
    private lateinit var database: AppDatabase
    private var clienteIdSeleccionado: Int? = null
    private var tipoComprobante: String = "Boleta"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistrarComprobanteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = AppDatabase.obtenerInstancia(this)
        configurarUI()
        cargarClientes()
        configurarAutoCompleteCliente()
    }

    private fun configurarAutoCompleteCliente() {
        lifecycleScope.launch {
            try {
                val clientes = withContext(Dispatchers.IO) {
                    database.clienteDAO().listar()
                }

                val adapter = ArrayAdapter(
                    this@RegistrarComprobante,
                    android.R.layout.simple_dropdown_item_1line,
                    clientes.map { "${it.nombre} (${it.numeroDocumento})" } // Muestra nombre + documento
                ).apply {
                    setNotifyOnChange(true)
                }

                binding.autoCompleteCliente.apply {
                    setAdapter(adapter)
                    threshold = 1 // Mostrar sugerencias desde el primer carácter tecleado

                    setOnItemClickListener { _, _, position, _ ->
                        clienteIdSeleccionado = clientes[position].id
                        cargarServiciosDelCliente(clientes[position].id)
                    }
                }
            } catch (e: Exception) {
                mostrarError("Error al cargar clientes")
            }
        }
    }

    private fun configurarUI() {
        // Configurar fecha actual
        binding.tvFechaActual.text = "Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}"

        // Configurar RadioGroup
        binding.radioGroupTipo.setOnCheckedChangeListener { _, checkedId ->
            tipoComprobante = if (checkedId == R.id.rbBoleta) "Boleta" else "Factura"
            actualizarVisibilidadCampos()
            calcularTotales()
        }

        // Configurar botón Guardar
        binding.btnGuardar.setOnClickListener {
            validarYGuardarComprobante()

        }
    }

    private fun cargarClientes() {
        lifecycleScope.launch {
            try {
                val clientes = withContext(Dispatchers.IO) {
                    database.clienteDAO().listar()
                }

                val adapter = ArrayAdapter(
                    this@RegistrarComprobante,
                    android.R.layout.simple_dropdown_item_1line,
                    clientes.map { it.nombre }
                )

                binding.autoCompleteCliente.setAdapter(adapter)

                binding.autoCompleteCliente.setOnItemClickListener { _, _, position, _ ->
                    clienteIdSeleccionado = clientes[position].id
                    cargarServiciosDelCliente(clientes[position].id)
                }
            } catch (e: Exception) {
                mostrarError("Error al cargar clientes")
            }
        }
    }

    private fun cargarServiciosDelCliente(clienteId: Int) {
        lifecycleScope.launch {
            try {
                val servicios = withContext(Dispatchers.IO) {
                    database.servicioDao().listarPorCliente(clienteId)
                }

                binding.linearServicios.removeAllViews()

                if (servicios.isEmpty()) {
                    binding.tvSinServicios.visibility = View.VISIBLE
                } else {
                    binding.tvSinServicios.visibility = View.GONE
                    servicios.forEach { servicio ->
                        val equipo = withContext(Dispatchers.IO) {
                            database.equipoDAO().buscarPorId(servicio.equipoId)
                        }

                        val checkBox = CheckBox(this@RegistrarComprobante).apply {
                            text = "${equipo?.marca} ${equipo?.modelo}\n${servicio.descripcion}\nS/ ${servicio.costo}"
                            tag = servicio
                            setOnCheckedChangeListener { _, _ ->
                                calcularTotales()
                            }
                        }
                        binding.linearServicios.addView(checkBox)
                    }
                }
            } catch (e: Exception) {
                mostrarError("Error al cargar servicios")
            }
        }
    }


    private fun obtenerServiciosSeleccionados(): List<Servicio> {
        val seleccionados = mutableListOf<Servicio>()
        for (i in 0 until binding.linearServicios.childCount) {
            val view = binding.linearServicios.getChildAt(i)
            if (view is CheckBox && view.isChecked) {
                val servicio = view.tag as Servicio
                if (servicio.costo > 0) { // Validar costo positivo
                    seleccionados.add(servicio)
                }
            }
        }
        return seleccionados
    }
    private fun calcularTotales() {
        try {
            val servicios = obtenerServiciosSeleccionados()
            val total = servicios.sumOf { it.costo }

            when (tipoComprobante) {
                "Factura" -> {
                    val subtotal = total / 1.18
                    val igv = total - subtotal

                    binding.apply {
                        txtSubTotal.text = "Subtotal: S/ ${"%.2f".format(subtotal)}"
                        txtIgv.text = "IGV: S/ ${"%.2f".format(igv)}"
                        txtTotal.text = "Total: S/ ${"%.2f".format(total)}"

                        txtSubTotal.visibility = View.VISIBLE
                        txtIgv.visibility = View.VISIBLE
                        txtTotal.visibility = View.VISIBLE
                    }
                }
                else -> { // Boleta
                    binding.apply {
                        txtTotal.text = "Total: S/ ${"%.2f".format(total)}"
                        txtSubTotal.visibility = View.GONE
                        txtIgv.visibility = View.GONE
                        txtTotal.visibility = View.VISIBLE
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CALCULO", "Error calculando totales", e)
        }
    }

    private fun actualizarVisibilidadCampos() {
        if (tipoComprobante == "Factura") {
            binding.txtSubTotal.visibility = View.VISIBLE
            binding.txtIgv.visibility = View.VISIBLE
        } else {
            binding.txtSubTotal.visibility = View.GONE
            binding.txtIgv.visibility = View.GONE
        }
    }

    private fun validarYGuardarComprobante() {
        if (clienteIdSeleccionado == null) {
            mostrarError("Seleccione un cliente")
            return
        }

        val servicios = obtenerServiciosSeleccionados()
        if (servicios.isEmpty()) {
            mostrarError("Seleccione al menos un servicio")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmar comprobante")
            .setMessage("¿Está seguro de registrar este comprobante?")
            .setPositiveButton("Confirmar") { _, _ ->
                guardarComprobante(servicios)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarComprobante(servicios: List<Servicio>) {
        lifecycleScope.launch {
            try {
                val total = servicios.sumOf { it.costo }
                val (subtotal, igv) = if (tipoComprobante == "Factura") {
                    val s = total / 1.18
                    val i = total - s
                    s to i
                } else {
                    0.0 to 0.0
                }

                val comprobante = Comprobante(
                    tipo = tipoComprobante,
                    clienteId = clienteIdSeleccionado!!,
                    fechaEmision = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    subtotal = subtotal,
                    igv = igv,
                    total = total,
                    totalDolares = 0.0 // Temporal, se actualizará con SUNAT después
                )

                val comprobanteId = withContext(Dispatchers.IO) {
                    database.comprobanteDAO().insertar(comprobante).toInt()
                }

                // Guardar relaciones con servicios
                servicios.forEach { servicio ->
                    withContext(Dispatchers.IO) {
                        database.comprobanteServicioDAO().insertar(
                            ComprobanteServicio(comprobanteId, servicio.id)
                        )
                    }
                }

                mostrarExito("Comprobante registrado exitosamente")
                cargarClientes()
                finish()
            } catch (e: Exception) {
                mostrarError("Error al guardar el comprobante")
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun mostrarExito(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

}