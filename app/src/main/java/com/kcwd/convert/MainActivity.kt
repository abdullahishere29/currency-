package com.kcwd.convert

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kcwd.convert.databinding.ActivityMainBinding
import com.kcwd.convert.network.ApiClient
import com.kcwd.convert.network.ApiService
import com.kcwd.convert.network.ExchangeRateResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var fromValue = 0.0
    private var fromUnit = ""
    private var toValue = 0.0
    private var toUnit = ""
    private val apiKey = "f80a7d78e920d5611974fada"

    private val defaultUnits = listOf(
        "USD", "EUR", "GBP", "INR", "AUD", "CAD", "SGD", "JPY", "CNY","PKR"
    )
    private val units = mutableListOf(
        "AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN", "BAM", "BBD", "BDT", "BGN", "BHD",
        "BIF", "BMD", "BND", "BOB", "BRL", "BSD", "BTN", "BWP", "BYN", "BZD", "CAD", "CDF", "CHF", "CLP", "CNY",
        "COP", "CRC", "CUP", "CVE", "CZK", "DJF", "DKK", "DOP", "DZD", "EGP", "ERN", "ETB", "EUR", "FJD", "FKP",
        "FOK", "GBP", "GEL", "GGP", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD", "HNL", "HRK", "HTG", "HUF",
        "IDR", "ILS", "IMP", "INR", "IQD", "IRR", "ISK", "JEP", "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KID",
        "KMF", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LYD", "MAD", "MDL", "MGA", "MKD",
        "MMK", "MNT", "MOP", "MRU", "MUR", "MVR", "MWK", "MXN", "MYR", "MZN", "NAD", "NGN", "NIO", "NOK", "NPR",
        "NZD", "OMR", "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD", "RUB", "RWF", "SAR",
        "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SLL", "SOS", "SRD", "SSP", "STN", "SYP", "SZL", "THB", "TJS",
        "TMT", "TND", "TOP", "TRY", "TTD", "TVD", "TWD", "TZS", "UAH", "UGX", "USD", "UYU", "UZS", "VES", "VND",
        "VUV", "WST", "XAF", "XCD", "XDR", "XOF", "XPF", "YER", "ZAR", "ZMW", "ZWL"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            fetchCurrencies()
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception during fetchCurrencies: ${e.message}", e)
            Toast.makeText(this, "Error during fetchCurrencies: ${e.message}", Toast.LENGTH_LONG).show()
        }

        binding.convertButton.setOnClickListener {
            try {
                fromValue = binding.fromValue.text.toString().toDouble()
                fromUnit = binding.fromUnit.selectedItem.toString()
                toUnit = binding.toUnit.selectedItem.toString()
                fetchRatesAndConvert()
            } catch (e: Exception) {
                Log.e("API_ERROR", "Exception during conversion: ${e.message}", e)
                Toast.makeText(this, "Conversion error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        setupSpinner(binding.fromUnit)
        setupSpinner(binding.toUnit)
    }

    private fun setupSpinner(spinner: Spinner) {
        spinner.setOnTouchListener { v, event ->
            binding.fromValue.clearFocus()
            binding.toValue.clearFocus()
            v.performClick()
            false
        }
    }

    private fun initComponents() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.fromUnit.adapter = adapter
        binding.toUnit.adapter = adapter
    }

    private fun fetchCurrencies() {
        val apiService = ApiClient.retrofit.create(ApiService::class.java)
        apiService.getRates(apiKey, "USD").enqueue(object : Callback<ExchangeRateResponse> {
            override fun onResponse(call: Call<ExchangeRateResponse>, response: Response<ExchangeRateResponse>) {
                if (response.isSuccessful) {
                    val rates = response.body()?.conversion_rates
                    if (rates != null) {
                        units.clear()
                        units.addAll(rates.keys)
                        initComponents()
                        Log.d("API_RESPONSE", "Rates fetched successfully: $rates")
                    } else {
                        Log.e("API_RESPONSE", "No rates available in response")
                        Toast.makeText(this@MainActivity, "No rates available", Toast.LENGTH_LONG).show()
                        initComponents()
                    }
                } else {
                    Log.e("API_RESPONSE", "Failed to fetch rates: ${response.errorBody()?.string()}")
                    Toast.makeText(this@MainActivity, "Failed to fetch rates", Toast.LENGTH_LONG).show()
                    initComponents()
                }
            }

            override fun onFailure(call: Call<ExchangeRateResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching rates", t)
                Toast.makeText(this@MainActivity, "Error fetching rates: ${t.message}", Toast.LENGTH_LONG).show()
                initComponents()
            }
        })
    }

    private fun fetchRatesAndConvert() {
        val apiService = ApiClient.retrofit.create(ApiService::class.java)
        apiService.getRates(apiKey, fromUnit).enqueue(object : Callback<ExchangeRateResponse> {
            override fun onResponse(call: Call<ExchangeRateResponse>, response: Response<ExchangeRateResponse>) {
                if (response.isSuccessful) {
                    val rates = response.body()?.conversion_rates
                    if (rates != null) {
                        val rate = rates[toUnit]
                        if (rate != null) {
                            toValue = fromValue * rate
                            binding.toValue.setText(toValue.toString())
                            Log.d("API_RESPONSE", "Conversion successful: $fromValue $fromUnit = $toValue $toUnit")
                        } else {
                            Log.e("API_RESPONSE", "Conversion rate for $toUnit not available")
                            Toast.makeText(this@MainActivity, "Conversion rate for $toUnit not available", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("API_RESPONSE", "No rates available in response")
                        Toast.makeText(this@MainActivity, "No rates available", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("API_RESPONSE", "Failed to fetch rates: ${response.errorBody()?.string()}")
                    Toast.makeText(this@MainActivity, "Failed to fetch rates", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ExchangeRateResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching rates", t)
                Toast.makeText(this@MainActivity, "Error fetching rates: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
