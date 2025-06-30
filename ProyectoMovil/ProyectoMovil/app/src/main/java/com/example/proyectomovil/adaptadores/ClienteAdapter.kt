package com.example.proyectomovil.adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectomovil.databinding.ItemClienteBinding
import com.example.proyectomovil.entidades.Cliente

class ClienteAdapter(
    private val listaClientes: MutableList<Cliente>,
    private val onItemLongClick: (Cliente) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    private var listaOriginal: List<Cliente> = listaClientes.toList()

    inner class ClienteViewHolder(val binding: ItemClienteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClienteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = listaClientes[position]
        holder.binding.apply {
            tvNombre.text = cliente.nombre
            tvTelefono.text = "Teléfono: ${cliente.telefono}"
            tvEmail.text = "Email: ${cliente.email}"
            tvDireccion.text = "Dirección: ${cliente.direccion}"
            tvNunero.text = "Número de Doc: ${cliente.numeroDocumento}"

            root.setOnLongClickListener {
                onItemLongClick(cliente)
                true
            }
        }
    }

    override fun getItemCount(): Int = listaClientes.size

    fun filtrar(texto: String) {
        val textoBuscar = texto.trim()
        val filtrada = if (textoBuscar.isEmpty()) {
            listaOriginal
        } else {
            listaOriginal.filter {
                it.nombre.contains(textoBuscar, ignoreCase = true) ||
                        it.email.contains(textoBuscar, ignoreCase = true) ||
                        it.telefono.contains(textoBuscar, ignoreCase = true)
            }
        }

        listaClientes.clear()
        listaClientes.addAll(filtrada)
        notifyDataSetChanged()
    }

    fun actualizarLista(nuevaLista: List<Cliente>) {
        listaClientes.clear()
        listaClientes.addAll(nuevaLista)
        listaOriginal = nuevaLista.toList() // sincroniza también la lista original
        notifyDataSetChanged()
    }
}
