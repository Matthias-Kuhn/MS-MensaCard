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
import android.widget.RemoteViews
import android.widget.Toast
import java.math.BigDecimal


class MainActivity : AppCompatActivity() {

    var url = "https://topup.klarna.com/api/v1/STW_MUNSTER/cards/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val sharedPref = getSharedPreferences("cardprefs",Context.MODE_PRIVATE) ?: return
        val nr = sharedPref.getInt("card_nr", -1)

        if (nr > 0) {
            findViewById<EditText>(R.id.editTextNumber).setText(nr.toString())
        }


        findViewById<Button>(R.id.button).setOnClickListener {
            storeNr(findViewById<EditText>(R.id.editTextNumber).text.toString().toInt())
            test()
            showAppWidget()
        }
    }

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
                } else {
                    Log.d("Emka - Tag", "responseBody is null")
                }

            }

            override fun onFailure(call: Call<BalanceResponse?>, t: Throwable) {
                Log.d("Emka - Tag", "onFailure: ")
            }
        })
        return result
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
                finish()
            }

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    }

    fun test(){
        val sharedPref = getSharedPreferences("cardprefs", Context.MODE_PRIVATE) ?: return
        val nr = sharedPref.getInt("card_nr", -1)
        val nUrl = "$url$nr/"
        val balance = getMyBalance(nUrl)

        Toast.makeText(this, intToString(balance), Toast.LENGTH_SHORT).show();




    }

    fun intToString(nr: Int): String {
        return BigDecimal(nr).movePointLeft(2).toString() + "€"
    }
}