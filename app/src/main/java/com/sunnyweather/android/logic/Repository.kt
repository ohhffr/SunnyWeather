package com.sunnyweather.android.logic

import androidx.lifecycle.liveData//是lifecycle-livedata-ktx库提供的一个非常强大且好用的功能

import com.sunnyweather.android.logic.network.SunnyWeatherNetWork
import kotlinx.coroutines.Dispatchers

//仓库层
//Repository层主要负责对数据的存取，它将数据源封装起来，向上层提供统一的接口，并决定调用哪个数据源，以及如何处理数据。
//主要工作就是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取，并将获得的数据返回给调用方,类似数据获取与缓存的中间层
//在本地没有缓存数据的情况下就去网络层获取，如果本地已经有缓存了，就直接将缓存数据返回。
object Repository {

    //liveData()函数，可以自动构建并返回一个LiveData对象，然后在它的代码块中提供一个挂起函数的上下文，就可以在liveData()函数的代码块中调用任意的挂起函数
    //将liveData()函数的线程参数类型指定成了Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中
    fun searchPlaces(query: String) = liveData(Dispatchers.IO){
        val result = try {
            //liveData()函数的代码块中调用了searchPlaces()函数，这个函数是一个挂起函数，它内部会调用网络请求的API，并返回一个Result对象，这个对象中封装了请求的结果。
            val placeResponse = SunnyWeatherNetWork.searchPlaces(query)
            if (placeResponse.status == "ok") { //服务器响应的状态是ok
                Result.success(placeResponse.places) //包装获取的城市数据列表
            }else{
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))//包装一个异常信息
            }
        }catch (e:Exception){
            Result.failure(e)
        }
        emit(result)//将包装的结果发射出去。类似于调用LiveData的 setValue()方法来通知数据变化
    }

}