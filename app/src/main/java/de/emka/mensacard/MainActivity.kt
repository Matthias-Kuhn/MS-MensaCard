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
            showAppWidget()
        }
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