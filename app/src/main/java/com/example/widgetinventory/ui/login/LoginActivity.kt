package com.example.widgetinventory.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.widgetinventory.MainActivity
import com.example.widgetinventory.R
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar el ejecutor
        executor = ContextCompat.getMainExecutor(this)

        // Configurar el diálogo de autenticación biométrica
        biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Autenticación exitosa", Toast.LENGTH_SHORT)
                        .show()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Cierra el login para que no puedan volver
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        // Configurar el contenido del cuadro emergente
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación con Biometría")
            .setSubtitle("Ingrese su huella digital")
            .setNegativeButtonText("Cancelar")
            .build()

        // Vincular la imagen de huella y activar la autenticación al presionarla
        val fingerprintImage = findViewById<LottieAnimationView>(R.id.fingerprintAnimation)
        fingerprintImage.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }
}