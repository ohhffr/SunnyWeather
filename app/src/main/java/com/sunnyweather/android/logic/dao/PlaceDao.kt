package com.sunnyweather.android.logic.dao

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

object PlaceDao {
    fun savePlace(place: Place) {//将Place对象存储到SharedPreferences文件中
        sharedPreferences().edit{
            putString("place", Gson().toJson(place))//通过GSON将Place对象转成一个JSON字符串，然后就可以用字符串存储的方式来保存数据了
        }
    }

    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")//我们先将JSON字符串从SharedPreferences文件中读取出来
        return Gson().fromJson(placeJson, Place::class.java)//通过GSON将JSON字符串解析成Place对象并返回。
    }

    fun isPlaceSaved() = sharedPreferences().contains("place")//判断是否有数据已被存储

    private fun sharedPreferences(): SharedPreferences = SunnyWeatherApplication.context
        .getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)

}