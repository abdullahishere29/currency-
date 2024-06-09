package com.kcwd.convert.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("{apiKey}/latest/{base}")
    fun getRates(
        @Path("apiKey") apiKey: String,
        @Path("base") base: String
    ): Call<ExchangeRateResponse>
}

data class ExchangeRateResponse(
    val conversion_rates: Map<String, Double>
)
