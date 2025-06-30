package com.example.proyectomovil.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectomovil.databinding.ItemComprobanteBinding
import com.example.proyectomovil.entidades.Comprobante
import com.example.proyectomovil.entidades.ComprobanteConCliente

class ComprobanteAdapter(
    private val context: Context,
    private val onEliminarClick: (Comprobante) -> Unit,
    private val onDetallesClick: (Comprobante) -> Unit
) : RecyclerView.Adapter<ComprobanteAdapter.ViewHolder>() {

    private var comprobantesOriginales = listOf<ComprobanteConCliente>()
    private var comprobantesFiltrados = listOf<ComprobanteConCliente>()

    inner class ViewHolder(val binding: ItemComprobanteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemComprobanteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comprobante = comprobantesFiltrados[position].comprobante
        val cliente = comprobantesFiltrados[position].cliente

        with(holder.binding) {
            tvTipo.text = comprobante.tipo
            tvFecha.text = comprobante.fechaEmision
            tvCliente.text = cliente?.nombre ?: "Cliente no encontrado"
            tvTotal.text = "Total: S/ ${"%.2f".format(comprobante.total)}"

            if (comprobante.tipo == "Factura") {
                tvSubtotal.text = "Subtotal: S/ ${"%.2f".format(comprobante.subtotal)}"
                tvIgv.text = "IGV: S/ ${"%.2f".format(comprobante.igv)}"
                tvSubtotal.visibility = View.VISIBLE
                tvIgv.visibility = View.VISIBLE
            } else {
                tvSubtotal.visibility = View.GONE
                tvIgv.visibility = View.GONE
            }

            btnEliminar.setOnClickListener { onEliminarClick(comprobante) }
            btnDetalles.setOnClickListener { onDetallesClick(comprobante) }
        }
    }

    override fun getItemCount() = comprobantesFiltrados.size

    fun actualizarLista(nuevosComprobantes: List<ComprobanteConCliente>) {
        comprobantesOriginales = nuevosComprobantes
        comprobantesFiltrados = nuevosComprobantes
        notifyDataSetChanged()
    }

    fun filtrarPorCliente(query: String) {
        comprobantesFiltrados = if (query.isEmpty()) {
            comprobantesOriginales
        } else {
            comprobantesOriginales.filter {
                it.cliente?.nombre?.contains(query, ignoreCase = true) ?: false
            }
        }
        notifyDataSetChanged()
    }

    fun removerComprobante(comprobante: Comprobante) {
        comprobantesOriginales = comprobantesOriginales.filter { it.comprobante.id != comprobante.id }
        comprobantesFiltrados = comprobantesFiltrados.filter { it.comprobante.id != comprobante.id }
        notifyDataSetChanged()
    }

}