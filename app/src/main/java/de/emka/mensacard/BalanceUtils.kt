package de.emka.mensacard

import de.emka.mensacard.data.BalanceApi
import de.emka.mensacard.data.BalanceResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal

class BalanceUtils {
    companion object {
        fun getBalanceResponse(url: String): Call<BalanceResponse> {
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
}