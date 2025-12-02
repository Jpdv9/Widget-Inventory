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

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        // --- NUEVO: Comprobación de sesión en onCreate ---
        // Comprueba si ya hay un usuario con sesión iniciada
        if (firebaseAuth.currentUser != null) {
            // Si es así, salta directamente a la actividad principal
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Cierra LoginActivity para que el usuario no pueda volver
            return   // Importante: Salimos de onCreate para no inflar la vista de login
        }

        // Si no hay sesión, continuamos y mostramos la pantalla de login
        setContentView(R.layout.activity_login)

        // --- (El resto de tu código onCreate no cambia) ---
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        errorPassword = findViewById(R.id.errorPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)


        btnLogin.isEnabled = false
        btnRegister.isEnabled = false
        inputEmail.addTextChangedListener(formWatcher)
        inputPassword.addTextChangedListener(formWatcher)

        // login
        btnLogin.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            btnLogin.isEnabled = false
            btnLogin.text = "INICIANDO..."

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        btnLogin.isEnabled = true
                        btnLogin.text = "INICIAR SESIÓN"
                    }
                }
        }

        // Acción de Registrarse
        btnRegister.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            btnRegister.isEnabled = false
            btnLogin.isEnabled = false
            btnRegister.text = "REGISTRANDO..."

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registro exitoso. Iniciando sesión...", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        btnRegister.isEnabled = true
                        btnLogin.isEnabled = true
                        btnRegister.text = "REGISTRARSE"
                    }
                }
        }
    }

    private val formWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateForm() }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun validateForm() {
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