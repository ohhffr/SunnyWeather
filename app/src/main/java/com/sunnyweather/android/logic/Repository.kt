package com.sunnyweather.android.logic

import android.content.Context
import androidx.lifecycle.liveData//是lifecycle-livedata-ktx库提供的一个非常强大且好用的功能
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather

import com.sunnyweather.android.logic.network.SunnyWeatherNetWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

//仓库层
//Repository层主要负责对数据的存取，它将数据源封装起来，向上层提供统一的接口，并决定调用哪个数据源，以及如何处理数据。
//主要工作就是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取，并将获得的数据返回给调用方,类似数据获取与缓存的中间层
//在本地没有缓存数据的情况下就去网络层获取，如果本地已经有缓存了，就直接将缓存数据返回。
object Repository {
    //liveData()函数，可以自动构建并返回一个LiveData对象，然后在它的代码块中提供一个挂起函数的上下文，就可以在liveData()函数的代码块中调用任意的挂起函数
    //将liveData()函数的线程参数类型指定成了Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中
    fun searchPlaces(query: String) = fire(Dispatchers.IO){
        //liveData()函数的代码块中调用了searchPlaces()函数，这个函数是一个挂起函数，它内部会调用网络请求的API，并返回一个Result对象，这个对象中封装了请求的结果。
        val placeResponse = SunnyWeatherNetWork.searchPlaces(query)
        if (placeResponse.status == "ok") { //服务器响应的状态是ok
            Result.success(placeResponse.places) //包装获取的城市数据列表
        }else{
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))//包装一个异常信息
        }
    }
    //异步刷新指定经纬度位置的天气数据
    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO){
        coroutineScope {//1、利用coroutineScope协程作用域构建器来创建了一个协程作用域，这个作用域内可以启动多个子协程，并且会等待所有子协程完成后才会继续执行后续代码。

            //2、并发请求天气数据，函数会同时发起两个网络请求，分别获取实时天气数据和每日天气数据
            val deferredRealtime = async {//利用async协程构建器，启动一个异步任务，并返回一个 Deferred 对象
                SunnyWeatherNetWork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetWork.getDailyWeather(lng, lat)
            }

            //3、await() 方法用于等待异步任务完成，并返回任务的结果
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()

            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {//服务器响应的状态是ok
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)//包装获取的天气数据
            } else {
                Result.failure(RuntimeException("response status is ${realtimeResponse.status} or ${dailyResponse.status}"))//包装一个异常信息
            }
        }
    }
    fun savePlace(place: Place) = fire(Dispatchers.IO){
        PlaceDao.savePlace(place)
        Result.success(place)
    }
    fun getSavedPlace() = fire(Dispatchers.IO){
        val place = PlaceDao.getSavedPlace()
        Result.success(place)
    }
    fun isPlaceSaved() = fire(Dispatchers.IO){
        val isSaved  = PlaceDao.isPlaceSaved()
        Result.success(isSaved)
    }

    //先调用一下liveData()函数,在liveData()函数的代码块中统一进行了try catch处理，并在try语句中调用传入的Lambda表达式中的代码，最终获取Lambda表达式的执行结果并调用emit()方法发射出去。
    private fun <T> fire(context: CoroutineContext, block: suspend() -> Result<T>) = liveData(context) {
        val result = try {
            block()
        }catch (e: Exception){
            Result.failure(e)
        }
        emit(result)//将包装的结果发射出去。类似于调用LiveData的setValue()方法来通知数据变化
    }

}


