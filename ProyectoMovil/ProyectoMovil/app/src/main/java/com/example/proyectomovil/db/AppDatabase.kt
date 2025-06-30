package com.example.proyectomovil.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.proyectomovil.dao.ClienteDAO
import com.example.proyectomovil.dao.ComprobanteDAO
import com.example.proyectomovil.dao.ComprobanteServicioDAO
import com.example.proyectomovil.dao.EquipoDAO
import com.example.proyectomovil.dao.ServicioDAO
import com.example.proyectomovil.dao.UsuarioDAO
import com.example.proyectomovil.entidades.Cliente
import com.example.proyectomovil.entidades.Comprobante
import com.example.proyectomovil.entidades.ComprobanteServicio
import com.example.proyectomovil.entidades.Equipo
import com.example.proyectomovil.entidades.Servicio
import com.example.proyectomovil.entidades.Usuario

@Database(
    entities = [
        Usuario::class,
        Cliente::class,
        Equipo::class,
        Servicio::class,
        Comprobante::class,
        ComprobanteServicio::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDAO(): UsuarioDAO
    abstract fun clienteDAO(): ClienteDAO
    abstract fun equipoDAO(): EquipoDAO
    abstract fun servicioDao(): ServicioDAO
    abstract fun comprobanteDAO(): ComprobanteDAO
    abstract fun comprobanteServicioDAO(): ComprobanteServicioDAO

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ServiceManagerRoom.db"
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
