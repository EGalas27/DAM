package com.example.proyectomovil.adaptadores

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectomovil.databinding.ItemEquipoBinding
import com.example.proyectomovil.entidades.Equipo

class EquipoAdapter(
    private var listaEquipos: List<Equipo>,
    private var mapaClientes: Map<Int, String>,
    private val onItemLongClick: (Equipo) -> Unit
) : RecyclerView.Adapter<EquipoAdapter.EquipoViewHolder>() {

    inner class EquipoViewHolder(val binding: ItemEquipoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipoViewHolder {
        val binding = ItemEquipoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        val equipo = listaEquipos[position]
        with(holder.binding) {
            tvMarca.text = "Marca: ${equipo.marca}"
            tvModelo.text = "Modelo: ${equipo.modelo}"
            tvProblema.text = "Problema: ${equipo.descripcionProblema}"
            tvCliente.text = "Cliente: ${mapaClientes[equipo.clienteId] ?: "Desconocido"}"

            if (equipo.imagen.isNotEmpty()) {
                try {
                    ivEquipo.setImageURI(Uri.parse(equipo.imagen))
                } catch (e: Exception) {
                    ivEquipo.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } else {
                ivEquipo.setImageResource(android.R.drawable.ic_menu_report_image)
            }

            root.setOnLongClickListener {
                onItemLongClick(equipo)
                true
            }
        }
    }

    override fun getItemCount(): Int = listaEquipos.size

    fun actualizarDatos(nuevaLista: List<Equipo>, nuevoMapaClientes: Map<Int, String>) {
        listaEquipos = nuevaLista
        mapaClientes = nuevoMapaClientes
        notifyDataSetChanged()
    }
}
