package com.example.widgetinventory.ui.login

import android.content.Intent
import android.content.Context
import android.content.SharedPreferences
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

    // Saber si la app se abrió desde el widget
    private var openedFromWidget = false

    companion object {
        private const val PREFS_NAME = "InventoryPrefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Saber si viene del widget
        openedFromWidget = intent.getBooleanExtra("fromWidget", false)

        // Si ya estaba logueado → ir directo al Home
        if (isSessionActive()) {
            navigateToHome()
            return
        }

        setContentView(R.layout.activity_login)

        // Inicializar el ejecutor
        executor = ContextCompat.getMainExecutor(this)

        // Configurar autenticación biométrica
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

                    saveSessionState(true)

                    // Redirigir al Home (venga del widget o no)
                    navigateToHome()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación con Biometría")
            .setSubtitle("Ingrese su huella digital")
            .setNegativeButtonText("Cancelar")
            .build()

        val fingerprintImage = findViewById<LottieAnimationView>(R.id.fingerprintAnimation)
        fingerprintImage.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    // Lleva al usuario al Home
    private fun navigateToHome() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
            putExtra("fromWidget", openedFromWidget)
        }
        startActivity(intent)
        finish()
    }

    private fun isSessionActive(): Boolean {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun saveSessionState(is_logged_in: Boolean) {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, is_logged_in).apply()
    }
}
