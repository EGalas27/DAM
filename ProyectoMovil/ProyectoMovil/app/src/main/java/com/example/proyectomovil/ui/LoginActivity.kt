package com.example.proyectomovil.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectomovil.MainActivity
import com.example.proyectomovil.databinding.ActivityLoginBinding
import com.example.proyectomovil.db.AppDatabase
import com.example.proyectomovil.util.DbSeeder
import com.example.proyectomovil.util.SeguridadPassword
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Insertar admin si no existe
        DbSeeder.insertarUsuarioAdmin(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(com.example.proyectomovil.R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ocultar la barra de progreso al inicio
        binding.progressBarLogin.visibility = View.GONE

        binding.btnLogin.setOnClickListener {
            val username = binding.edtUsuario.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Snackbar.make(binding.root, "⚠️ Por favor complete los campos", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.parseColor("#FFA000"))
                    .setTextColor(Color.BLACK)
                    .show()
            } else {
                val db = AppDatabase.obtenerInstancia(this)
                val usuario = db.usuarioDAO().buscarPorUsername(username)

                if (usuario != null && SeguridadPassword.hashSHA256(password) == usuario.password) {
                    // Mostrar ProgressBar durante 3 segundos
                    binding.progressBarLogin.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false

                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.progressBarLogin.visibility = View.GONE
                        binding.btnLogin.isEnabled = true

                        // Mostrar mensaje de bienvenida y pasar al menú al aceptar
                        Snackbar.make(binding.root, "✅ Bienvenido ${usuario.nombre}", Snackbar.LENGTH_INDEFINITE)
                            .setBackgroundTint(Color.parseColor("#4CAF50"))
                            .setTextColor(Color.WHITE)
                            .setAction("Aceptar") {
                                val intent = Intent(this, MainActivity::class.java).apply {
                                    putExtra("nombre", usuario.nombre)
                                    putExtra("username", usuario.username)
                                }
                                startActivity(intent)
                                finish()
                            }
                            .setActionTextColor(Color.YELLOW)
                            .show()
                    }, 3000)

                } else {
                    // Credenciales incorrectas
                    Snackbar.make(binding.root, "❌ Credenciales incorrectas", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(Color.parseColor("#F44336"))
                        .setTextColor(Color.WHITE)
                        .setAction("Reintentar") {}
                        .setActionTextColor(Color.YELLOW)
                        .show()

                    // Limpiar los campos
                    binding.edtUsuario.setText("")
                    binding.edtPassword.setText("")
                }
            }
        }
    }
}
