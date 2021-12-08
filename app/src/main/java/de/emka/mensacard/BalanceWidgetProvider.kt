package de.emka.mensacard

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


class BalanceWidgetProvider: AppWidgetProvider() {
    var url = "https://topup.klarna.com/api/v1/STW_MUNSTER/cards/"

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        val sharedPref = context!!.getSharedPreferences("cardprefs", Context.MODE_PRIVATE) ?: return
        val nr = sharedPref.getInt("card_nr", -1)
        val nUrl = "$url$nr/"
        val balance = getMyBalance(nUrl)
        appWidgetIds!!.forEach { appWidgetId ->
            val textViews: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.balance_widget
            ).apply {
                setTextViewText(R.id.tv_balance, intToString(balance))
                Toast.makeText(context!!, intToString(balance), Toast.LENGTH_SHORT);

                //etOnClickPendingIntent(R.id.tv_balance, open())

            }

            appWidgetManager!!.updateAppWidget(appWidgetId, textViews)

        }
    }

//    override fun onUpdate(
//        context: Context,
//        appWidgetManager: AppWidgetManager,
//        appWidgetIds: IntArray
//    ) {
//
//        val sharedPref = context.getSharedPreferences("cardprefs", Context.MODE_PRIVATE) ?: return
//        val nr = sharedPref.getInt("card_nr", -1)
//        val nUrl = "$url$nr/"
//        val balance = getMyBalance(nUrl)
//        appWidgetIds.forEach { appWidgetId ->
//            val textViews: RemoteViews = RemoteViews(
//                context.packageName,
//                R.layout.balance_widget
//            ).apply {
//                setTextViewText(R.id.tv_balance, intToString(balance))
//
//                //etOnClickPendingIntent(R.id.tv_balance, open())
//
//            }
//
//            appWidgetManager.updateAppWidget(appWidgetId, textViews)
//
//        }
//    }

    private fun getMyBalance(url: String): Int {
        var result = -1
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .build()
            .create(BalanceApi::class.java)
        val retrofitData = retrofitBuilder.getBalance()

        retrofitData.enqueue(object : Callback<BalanceResponse?> {
            override fun onResponse(
                call: Call<BalanceResponse?>,
                response: Response<BalanceResponse?>
            ) {
                val responseBody = response.body()

                if(responseBody != null) {
                    result = responseBody.balance
                    Log.d("Emka - Tag", "onResponse: ${responseBody.balance}")
                }
                Log.d("Emka - Tag", "responseBody is null")
            }

            override fun onFailure(call: Call<BalanceResponse?>, t: Throwable) {
                Log.d("Emka - Tag", "onFailure: ")
            }
        })
        return result
    }

    fun intToString(nr: Int): String {
        return BigDecimal(nr).movePointLeft(2).toString() + "€"
    }

}