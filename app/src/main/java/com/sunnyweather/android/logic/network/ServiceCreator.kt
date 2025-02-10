package com.sunnyweather.android.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Retrofit构建器
object ServiceCreator {
    private const val BASE_URL = "https://api.caiyunapp.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    //接收一个 Class 对象作为参数，通过 Retrofit 来创建对应的服务接口实例。
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    //内联函数，使用了具体化类型参数 reified，可以直接通过泛型来创建服务接口实例，无需显式传递 Class 对象。
    inline fun <reified T> create(): T = create(T::class.java)
}