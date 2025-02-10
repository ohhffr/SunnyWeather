package com.sunnyweather.android.ui.weather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location
import com.sunnyweather.android.logic.model.Weather

class WeatherViewModel : ViewModel() {
    private val locationLiveData = MutableLiveData<Location>()

    //和界面相关的数据
    var locationLng = ""
    var locationLat = ""
    var placeName = ""

    val weatherLiveData: LiveData<Result<Weather>> = locationLiveData.switchMap { location -> //观察这个对象
        Repository.refreshWeather(location.lng, location.lat) //调用仓库层中定义的refreshWeather()方法
        //仓库层返回的LiveData对象就可以转换成一个可供Activity观察的LiveData对象了
    }

    fun refreshWeather(lng: String, lat: String) {//刷新天气信息
        locationLiveData.value = Location(lng, lat) //传入的经纬度参数封装成一个Location对象后赋值给locationLiveData对象
    }
}