package com.example.widgetinventory

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking
import android.widget.RemoteViews
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

        // Configurar clic en el ícono de gestión (irá a MainActivity)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_manage, pendingIntent)

        // Configurar clic en el ícono del ojo
        views.setOnClickPendingIntent(R.id.eye_icon, getToggleBalanceIntent(context, appWidgetId))

        // Estado inicial del saldo (oculto)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isBalanceVisible = prefs.getBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", false)

        if (isBalanceVisible) {
            val balance = calculateTotalBalance(context)
            views.setTextViewText(R.id.txt_balance, "$${formatBalance(balance)}")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_closed)
        } else {
            views.setTextViewText(R.id.txt_balance, "$ ****")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_open)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

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

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == TOGGLE_BALANCE_ACTION) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                toggleBalance(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
    }

    private fun toggleBalance(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_inventory)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isBalanceVisible = prefs.getBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", false)

        if (isBalanceVisible) {
            // Ocultar saldo
            views.setTextViewText(R.id.txt_balance, "$ ****")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_open)
            prefs.edit().putBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", false).apply()
        } else {
            // Mostrar saldo
            val balance = calculateTotalBalance(context)
            views.setTextViewText(R.id.txt_balance, "$${formatBalance(balance)}")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_closed)
            prefs.edit().putBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", true).apply()
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun calculateTotalBalance(context: Context): Double {
        return try {
            val database = InventoryDatabase.getDatabase(context)

            // Usar runBlocking para llamar a la función suspend desde el widget
            return runBlocking {
                database.productDao().getTotalBalance() ?: 0.0
            }
        } catch (e: Exception) {
            // Si hay error, retornar 0.0
            0.0
        }
    }

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