package com.example.proyectomovil.adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectomovil.databinding.ItemServicioBinding
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.entidades.ServicioConDetalles
import com.example.proyectomovil.ui.RegistrarServicio
import com.google.android.material.snackbar.Snackbar

class ServicioAdapter(
    private val context: Context,
    private var lista: MutableList<ServicioConDetalles>,
    private val onActualizarLista: () -> Unit
) : RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder>() {

    inner class ServicioViewHolder(val binding: ItemServicioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val binding = ItemServicioBinding.inflate(LayoutInflater.from(context), parent, false)
        return ServicioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        val servicio = lista[position]

        holder.binding.tvFechaServicio.text = servicio.fecha
        holder.binding.tvEquipoServicio.text = "Equipo: ${servicio.nombreEquipo}"
        holder.binding.tvTecnicoServicio.text = "Técnico: ${servicio.nombreTecnico}"
        holder.binding.tvCategoriaServicio.text = "Categoría: ${servicio.categoria}"
        holder.binding.tvDescripcionServicio.text = "Descripción: ${servicio.descripcion}"
        holder.binding.tvEstadoServicio.text = "Estado: ${servicio.estado}"
        holder.binding.tvCostoServicio.text = "Costo: S/ ${servicio.costo}"

        //  Clic largo para mostrar opciones: Editar o Eliminar
        holder.itemView.setOnLongClickListener {
            val opciones = arrayOf("Editar", "Eliminar")
            AlertDialog.Builder(context)
                .setTitle("Opciones del servicio")
                .setItems(opciones) { _, which ->
                    when (which) {
                        0 -> {
                            // EDITAR
                            val intent = Intent(context, RegistrarServicio::class.java)
                            intent.putExtra("servicio_id", servicio.id)
                            context.startActivity(intent)
                        }
                        1 -> {
                            // ELIMINAR CON CONFIRMACIÓN
                            AlertDialog.Builder(context)
                                .setTitle("¿Deseas eliminar este servicio?")
                                .setMessage("Esta acción no se puede deshacer.")
                                .setPositiveButton("Eliminar") { _, _ ->
                                    val db = AppDatabase.obtenerInstancia(context)
                                    db.servicioDao().eliminarPorId(servicio.id)
                                    lista.removeAt(position)
                                    notifyItemRemoved(position)
                                    Snackbar.make(holder.itemView, "✅ Servicio eliminado correctamente", Snackbar.LENGTH_SHORT).show()
                                    onActualizarLista()
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        }
                    }
                }
                .show()
            true
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<ServicioConDetalles>) {
        lista = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }
}
