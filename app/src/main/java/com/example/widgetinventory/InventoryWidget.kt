package com.example.widgetinventory

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.widgetinventory.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class InventoryWidget : AppWidgetProvider() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Actualizar todos los widgets
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_inventory)
        val user = auth.currentUser

        // --- GESTIONAR (Login o Home) ---
        val loginIntent = Intent(context, LoginActivity::class.java).apply {
            putExtra("fromWidget", true)
        }

        val homeIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("fromWidget", true)
        }

        val pendingManage = if (user == null) {
            PendingIntent.getActivity(
                context, 10, loginIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context, 11, homeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        views.setOnClickPendingIntent(R.id.btn_manage, pendingManage)
        views.setOnClickPendingIntent(R.id.txt_manage, pendingManage)



        // Si no hay usuario logueado el saldo estara oculto
        if (user == null) {
            views.setTextViewText(R.id.txt_balance, "$ ****")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_open)

            // El ojo NO hace nada si no ha iniciado sesión
            views.setOnClickPendingIntent(R.id.eye_icon, null)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }

        //  Si sí hay usuario logueado → permitir mostrar/ocultar
        views.setOnClickPendingIntent(R.id.eye_icon, getToggleBalanceIntent(context, appWidgetId))

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isVisible = prefs.getBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", false)

        if (isVisible) {
            CoroutineScope(Dispatchers.IO).launch {
                val balance = calculateTotalBalance(context)
                withContext(Dispatchers.Main) {
                    views.setTextViewText(R.id.txt_balance, "$${formatBalance(balance)}")
                    views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_closed)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        } else {
            views.setTextViewText(R.id.txt_balance, "$ ****")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_open)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    // Devuelve el PendingIntent para el Broadcast del "Ojo"
    private fun getToggleBalanceIntent(context: Context, appWidgetId: Int): PendingIntent {
        val user = auth.currentUser

        return if (user == null) {
            // ❗ Usuario NO logueado → abrir LoginActivity (NO broadcast)
            val loginIntent = Intent(context, LoginActivity::class.java).apply {
                putExtra("fromWidget", true)
            }

            PendingIntent.getActivity(
                context,
                999, // requestCode único
                loginIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        } else {
            // ✔ Usuario logueado → enviar BROADCAST al widget para hacer toggle
            val intent = Intent(context, InventoryWidget::class.java).apply {
                // Así se establece la acción correctamente
                action = TOGGLE_BALANCE_ACTION
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }



    // Recibe el Broadcast del "Ojo"
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == TOGGLE_BALANCE_ACTION) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // Llama a la función que actualiza el estado y la UI
                toggleBalance(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
    }

    // Cambia el estado (visible/oculto) y actualiza la UI
    private fun toggleBalance(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_inventory)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isBalanceVisible = prefs.getBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", false)

        if (isBalanceVisible) {
            // Si era visible, OCULTAR
            views.setTextViewText(R.id.txt_balance, "$ ****")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_open)
            prefs.edit().putBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", false).apply()
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } else {
            // Si estaba oculto, MOSTRAR
            CoroutineScope(Dispatchers.IO).launch {
                val balance = calculateTotalBalance(context)
                withContext(Dispatchers.Main) {
                    views.setTextViewText(R.id.txt_balance, "$${formatBalance(balance)}")
                    views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_closed)
                    prefs.edit().putBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", true).apply()
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    // Calcula el total del inventario
    private suspend fun calculateTotalBalance(context: Context): Double {
        return try {
            val snapshot = db.collection("products").get().await()
            var total = 0.0

            for (doc in snapshot.documents) {
                val price = doc.getDouble("price") ?: 0.0
                val quantity = doc.getLong("quantity")?.toInt() ?: 0
                total += price * quantity
            }

            total
        } catch (e: Exception) {
            0.0
        }
    }


    // Formatea el saldo
    private fun formatBalance(balance: Double): String {
        val formatter = DecimalFormat("#,###.00", DecimalFormatSymbols(Locale.ENGLISH))
        return formatter.format(balance).replace(",", "X").replace(".", ",").replace("X", ".")
    }

    companion object {
        const val TOGGLE_BALANCE_ACTION = "TOGGLE_BALANCE_ACTION"
        const val PREFS_NAME = "InventoryWidgetPrefs"
        const val BALANCE_VISIBLE_KEY = "balance_visible_"
    }
}