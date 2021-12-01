package de.emka.mensacard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import de.emka.mensacard.data.BalanceApi
import de.emka.mensacard.data.BalanceResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import android.appwidget.AppWidgetManager
import android.content.Context

import android.content.Intent
import android.widget.Button
import android.widget.EditText


class MainActivity : AppCompatActivity() {

    var url = "https://topup.klarna.com/api/v1/STW_MUNSTER/cards/2070000/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getMyBalance()

        val sharedPref = getSharedPreferences("cardprefs",Context.MODE_PRIVATE) ?: return
        val nr = sharedPref.getInt("card_nr", -1)

        if (nr > 0) {
            findViewById<EditText>(R.id.editTextNumber).setText(nr.toString())
        }


        findViewById<Button>(R.id.button).setOnClickListener {
            storeNr(findViewById<EditText>(R.id.editTextNumber).text.toString().toInt())
            showAppWidget()
        }
    }

    private fun getMyBalance() {
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
                val responseBody = response.body()!!
                Log.d("Emka - Tag", "onResponse: ${responseBody.balance}")
            }

            override fun onFailure(call: Call<BalanceResponse?>, t: Throwable) {
                Log.d("Emka - Tag", "onFailure: ")
            }
        })
    }

    fun storeNr(nr: Int){
        val sharedPref = getSharedPreferences("cardprefs",Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt("card_nr", nr)
            apply()
        }

    }

    fun showAppWidget() {
        var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;


        val intent = getIntent();
        val extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                finish();
            }

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    }
}