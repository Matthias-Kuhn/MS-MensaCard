package de.emka.mensacard

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import de.emka.mensacard.data.BalanceApi
import de.emka.mensacard.data.BalanceResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


import android.app.PendingIntent
import android.content.ComponentName

import android.content.Intent

class BalanceWidgetProvider: AppWidgetProvider() {
    val BASE_URL = "https://api.topup.klarna.com/api/v1/STW_MUNSTER/cards/"

    // called when widget for BalanceWidgetProvider is instantiated. (e.g. boot)
    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widget = ComponentName(context!!.packageName, BalanceWidgetProvider::class.java.name)
        val widgetIds = appWidgetManager.getAppWidgetIds(widget)

        onUpdate(context, appWidgetManager, widgetIds)
    }


    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        val sharedPref = context!!.getSharedPreferences("cardprefs", Context.MODE_PRIVATE) ?: return
        val nr = sharedPref.getInt("card_nr", -1)
        val nUrl = "$BASE_URL$nr/"

        val retrofitData = getBalanceResponse(nUrl)

        // update widget onClick with PendingIntent
        appWidgetIds!!.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.balance_widget).apply {
                val intentUpdate = Intent(context, BalanceWidgetProvider::class.java)
                intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val idArray = intArrayOf(appWidgetId)
                intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray)
                val pendingUpdate = PendingIntent.getBroadcast(
                    context, 0, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT)
                setOnClickPendingIntent(R.id.bg, pendingUpdate)
            }
            appWidgetManager!!.updateAppWidget(appWidgetId, views)
        }


        // handle received retrofit data
        retrofitData.enqueue(object : Callback<BalanceResponse?> {
            @SuppressLint("RemoteViewLayout")
            override fun onResponse(
                call: Call<BalanceResponse?>,
                response: Response<BalanceResponse?>
            ) {
                val responseBody = response.body()

                if(responseBody != null) {
                    val balance = responseBody.balance
                    val sdf = SimpleDateFormat("dd.MM   HH:mm")
                    val currentDate = sdf.format(Date())
                    Log.d("Emka - Tag", "onResponse: ${responseBody.balance}")
                    with (sharedPref.edit()) {
                        putString("balance", intToString(balance))
                        putString("date", currentDate)
                        apply()
                    }

                    // update textViews on every widget
                    appWidgetIds.forEach { appWidgetId ->
                        val textViews: RemoteViews = RemoteViews(
                            context.packageName,
                            R.layout.balance_widget
                        ).apply {
                            setTextViewText(R.id.tv_balance, intToString(balance))
                            setTextViewText(R.id.tv_date, currentDate)
                        }
                        appWidgetManager!!.updateAppWidget(appWidgetId, textViews)
                    }
                } else {
                    // e.g. wrong card nr
                    Log.d("Emka - Tag", "responseBody is null")

                }
            }

            override fun onFailure(call: Call<BalanceResponse?>, t: Throwable) {
                Log.d("Emka - Tag", "onFailure: ")
                val balanceString = sharedPref.getString("balance", "Fehler")
                val dateString = sharedPref.getString("date", "-")
                appWidgetIds.forEach { appWidgetId ->
                    val textViews: RemoteViews = RemoteViews(
                        context.packageName,
                        R.layout.balance_widget
                    ).apply {
                        setTextViewText(R.id.tv_balance, balanceString)
                        setTextViewText(R.id.tv_date, dateString)
                    }

                    appWidgetManager!!.updateAppWidget(appWidgetId, textViews)

                }
            }
        })
    }

    // receive PendingIntent from onClick
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d("Emka - Tag", "onReceive: called")

        if (intent!= null){
            val extras = intent.extras
            if (extras!= null) {
                Log.d("Emka - Tag", "onReceive: called - inner")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widget = ComponentName(context!!.packageName, BalanceWidgetProvider::class.java.name)
                val widgetIds = appWidgetManager.getAppWidgetIds(widget)

                onUpdate(context, appWidgetManager, widgetIds)
            }
        }
    }


    private fun getBalanceResponse(url: String): Call<BalanceResponse> {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .build()
            .create(BalanceApi::class.java)
        return retrofitBuilder.getBalance()
    }

    /**
     * Convert the balance in cents to a formatted String
     */
    fun intToString(balance: Int): String {
        return BigDecimal(balance).movePointLeft(2).toString() + "€"
    }

}