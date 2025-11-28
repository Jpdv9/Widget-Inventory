package com.example.widgetinventory

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.widgetinventory.data.db.InventoryDatabase
import com.example.widgetinventory.data.repository.ProductRepository
import com.example.widgetinventory.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class InventoryWidget : AppWidgetProvider() {

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

        //  Intent para "Gestionar" (Apunta a LoginActivity) ---
        val loginIntent = Intent(context, LoginActivity::class.java)
        val loginPendingIntent = PendingIntent.getActivity(
            context,
            0,
            loginIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Aplicar a ambas vistas (icono y texto)
        views.setOnClickPendingIntent(R.id.btn_manage, loginPendingIntent)
        views.setOnClickPendingIntent(R.id.txt_manage, loginPendingIntent)

        // Intent para el "Ojo"
        views.setOnClickPendingIntent(R.id.eye_icon, getToggleBalanceIntent(context, appWidgetId))

        // Lógica de visibilidad (Usa Corrutinas)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isBalanceVisible = prefs.getBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", false)

        if (isBalanceVisible) {
            // Mostrar saldo
            CoroutineScope(Dispatchers.IO).launch {
                val balance = calculateTotalBalance(context) // Llama a 'suspend'
                withContext(Dispatchers.Main) {
                    views.setTextViewText(R.id.txt_balance, "$${formatBalance(balance)}")
                    views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_closed)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        } else {
            // Ocultar saldo
            views.setTextViewText(R.id.txt_balance, "$ ****")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_open)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    // Devuelve el PendingIntent para el Broadcast del "Ojo"
    private fun getToggleBalanceIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, InventoryWidget::class.java).apply {
            action = TOGGLE_BALANCE_ACTION
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
            val database = InventoryDatabase.getDatabase(context)
            // Usamos el Repositorio
            val repository = ProductRepository(database.productDao())
            val products = repository.getProductsForWidget()

            // Lógica de suma correcta
            var totalBalance = 0.0
            products.forEach { product ->
                totalBalance += (product.price * product.quantity)
            }
            totalBalance
        } catch (e: Exception) {
            0.0 // Devuelve 0 si hay un error
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