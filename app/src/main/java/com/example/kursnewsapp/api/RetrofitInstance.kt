package com.example.kursnewsapp.api

import com.example.kursnewsapp.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object {

        private val retrofit by lazy{
            val logging=HttpLoggingInterceptor()
            logging.level=HttpLoggingInterceptor.Level.BODY

            val client=OkHttpClient.Builder().addInterceptor(logging).build()

            Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        //к этому обращаемся в repositore!!!(типо как корутины в бд!!!)
        val api by lazy {
            retrofit.create(newsAPI::class.java)
        }
    }
}