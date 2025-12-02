package com.example.widgetinventory.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.widgetinventory.MainActivity
import com.example.widgetinventory.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputPassword: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var errorPassword: TextView
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: TextView

    // 1. Añadimos una instancia de FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 2. Inicializamos Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // ... (inicialización de vistas no cambia)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        errorPassword = findViewById(R.id.errorPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // ... (validaciones no cambian)
        btnLogin.isEnabled = false
        btnRegister.isEnabled = false
        inputEmail.addTextChangedListener(formWatcher)
        inputPassword.addTextChangedListener(formWatcher)

        // 3. Modificamos la acción de Login
        btnLogin.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            // Deshabilitamos el botón para dar feedback al usuario
            btnLogin.isEnabled = false
            btnLogin.text = "INICIANDO..."

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Si el login es exitoso, navegamos a la pantalla principal
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Cerramos la LoginActivity para que el usuario no pueda volver
                    } else {
                        // Si el login falla, mostramos un error y reactivamos el botón
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        btnLogin.isEnabled = true
                        btnLogin.text = "INICIAR SESIÓN"
                    }
                }
        }

        // ... (el resto del archivo no cambia)
        btnRegister.setOnClickListener {
            Toast.makeText(this, "Ir a pantalla de registro...", Toast.LENGTH_SHORT).show()
        }
    }

    private val formWatcher = object : TextWatcher {
        // ... (código del watcher no cambia)
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateForm() }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun validateForm() {
        // ... (código de validación no cambia)
        val email = inputEmail.text.toString().trim()
        val password = inputPassword.text.toString().trim()
        if (password.length in 1..5) {
            errorPassword.text = "Mínimo 6 dígitos"
            errorPassword.visibility = TextView.VISIBLE
            passwordLayout.boxStrokeColor = getColor(android.R.color.holo_red_dark)
        } else {
            errorPassword.visibility = TextView.GONE
            passwordLayout.boxStrokeColor = getColor(android.R.color.white)
        }
        val validForm = email.isNotEmpty() && password.length in 6..10
        btnLogin.isEnabled = validForm
        btnRegister.isEnabled = email.isNotEmpty() && password.isNotEmpty()
    }
}