package de.emka.mensacard

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import de.emka.mensacard.data.BalanceResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.appwidget.AppWidgetManager
import android.content.Context

import android.content.Intent
import android.widget.*


class MainActivity : AppCompatActivity() {
    val BASE_URL = "https://api.topup.klarna.com/api/v1/STW_MUNSTER/cards/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("cardprefs",Context.MODE_PRIVATE) ?: return
        val nr = sharedPref.getInt("card_nr", -1)
        val nUrl = "$BASE_URL$nr/"

        val retrofitData = BalanceUtils.getBalanceResponse(nUrl)

        if (nr > 0) {
            findViewById<EditText>(R.id.editTextNumber).setText(nr.toString())

            getAndShowBalance(retrofitData)
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            val nrB = findViewById<EditText>(R.id.editTextNumber).text.toString().toIntOrNull()
            if (nrB != null) {
                storeNr(nrB)
            }
            val nUrlB = "$BASE_URL$nrB/"
            val retrofitDataB = BalanceUtils.getBalanceResponse(nUrlB)
            getAndShowBalance(retrofitDataB)
            showAppWidget()
        }
    }

    private fun getAndShowBalance(retrofitData: Call<BalanceResponse>) {
        retrofitData.enqueue(object : Callback<BalanceResponse?> {
            @SuppressLint("RemoteViewLayout")
            override fun onResponse(
                call: Call<BalanceResponse?>,
                response: Response<BalanceResponse?>
            ) {
                val responseBody = response.body()

                if (responseBody != null) {
                    val balance = responseBody.balance
                    val displayText = getString(R.string.currentBalance) + BalanceUtils.intToString(balance)
                    findViewById<TextView>(R.id.textView2).text = displayText
                } else {
                    findViewById<TextView>(R.id.textView2).text = getString(R.string.balanceNotAvailable)
                }
            }

            override fun onFailure(call: Call<BalanceResponse?>, t: Throwable) {
                findViewById<TextView>(R.id.textView2).text = getString(R.string.balanceNotAvailable)
            }
        })
    }


    private fun storeNr(nr: Int){
        val sharedPref = getSharedPreferences("cardprefs",Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt("card_nr", nr)
            apply()
        }

    }

    private fun showAppWidget() {
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            val appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                finish()
            }

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}