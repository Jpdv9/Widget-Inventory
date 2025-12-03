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
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val pendingResult = goAsync()
            updateAppWidget(context, appWidgetManager, appWidgetId, pendingResult)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        pendingResult: PendingResult
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_inventory)
        val user = auth.currentUser

        val manageIntent = if (user == null) {
            Intent(context, LoginActivity::class.java)
        } else {
            Intent(context, MainActivity::class.java)
        }
        val pendingManage = PendingIntent.getActivity(context, appWidgetId, manageIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.btn_manage, pendingManage)
        views.setOnClickPendingIntent(R.id.txt_manage, pendingManage)

        if (user == null) {
            views.setTextViewText(R.id.txt_balance, "$ ****")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_open)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            pendingResult.finish()
            return
        }

        views.setOnClickPendingIntent(R.id.eye_icon, getToggleBalanceIntent(context, appWidgetId))

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isVisible = prefs.getBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", false)

        if (isVisible) {
            scope.launch {
                try {
                    val balance = calculateTotalBalance()
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.txt_balance, "$${formatBalance(balance)}")
                        views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_closed)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        } else {
            views.setTextViewText(R.id.txt_balance, "$ ****")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_open)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            pendingResult.finish()
        }
    }

    private fun getToggleBalanceIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, InventoryWidget::class.java).apply {
            action = TOGGLE_BALANCE_ACTION
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == TOGGLE_BALANCE_ACTION) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val pendingResult = goAsync()
                toggleBalance(context, appWidgetId, pendingResult)
            }
        }
    }

    private fun toggleBalance(context: Context, appWidgetId: Int, pendingResult: PendingResult) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val views = RemoteViews(context.packageName, R.layout.widget_inventory)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isBalanceVisible = prefs.getBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", false)

        if (isBalanceVisible) {
            views.setTextViewText(R.id.txt_balance, "$ ****")
            views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_open)
            prefs.edit().putBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", false).apply()
            appWidgetManager.updateAppWidget(appWidgetId, views)
            pendingResult.finish()
        } else {
            scope.launch {
                try {
                    val balance = calculateTotalBalance()
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.txt_balance, "$${formatBalance(balance)}")
                        views.setImageViewResource(R.id.eye_icon, R.drawable.ic_eye_closed)
                        prefs.edit().putBoolean("$BALANCE_VISIBLE_KEY$appWidgetId", true).apply()
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun calculateTotalBalance(): Double {
        val user = auth.currentUser ?: return 0.0
        return try {
            val snapshot = db.collection("products")
                .whereEqualTo("userId", user.uid)
                .get()
                .await()
            snapshot.documents.sumOf { doc ->
                val price = doc.getDouble("price") ?: 0.0
                val quantity = doc.getLong("quantity")?.toInt() ?: 0
                price * quantity
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    private fun formatBalance(balance: Double): String {
        val formatter = DecimalFormat("#,###.00", DecimalFormatSymbols(Locale.ENGLISH))
        return formatter.format(balance).replace(",", "X").replace(".", ",").replace("X", ".")
    }

    companion object {
        const val TOGGLE_BALANCE_ACTION = "com.example.widgetinventory.TOGGLE_BALANCE_ACTION"
        const val PREFS_NAME = "InventoryWidgetPrefs"
        const val BALANCE_VISIBLE_KEY = "balance_visible_"
    }
}
