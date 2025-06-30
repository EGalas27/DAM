package com.example.proyectomovil.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.proyectomovil.entidades.ComprobanteServicio
import com.example.proyectomovil.entidades.Servicio

@Dao
interface ComprobanteServicioDAO {
    @Insert
    fun insertar(comprobanteServicio: ComprobanteServicio)

    @Query("SELECT servicio.* FROM servicio " +
            "INNER JOIN ComprobanteServicio ON servicio.id = ComprobanteServicio.servicioId " +
            "WHERE ComprobanteServicio.comprobanteId = :comprobanteId")
    fun obtenerServiciosPorComprobante(comprobanteId: Int): List<Servicio>

    @Query("DELETE FROM ComprobanteServicio WHERE comprobanteId = :comprobanteId")
     fun eliminarPorComprobante(comprobanteId: Int)

}