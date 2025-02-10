package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.PlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceService {
    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")
    //返回值被声明成了Call<PlaceResponse>，将服务器返回的JSON数据自动解析成PlaceResponse对象
    fun searchPlaces(@Query("query") query: String): Call<PlaceResponse> //query这个参数是需要动态指定的，@Query注解的方式来进行实现
}