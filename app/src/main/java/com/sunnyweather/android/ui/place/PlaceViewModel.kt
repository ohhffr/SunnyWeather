package com.sunnyweather.android.ui.place

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

//原则上与界面相关的数据都应该放到ViewModel
class PlaceViewModel : ViewModel(){

    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>() //用于对界面上显示的城市数据进行缓存,保证数据在手机屏幕发生旋转的时候不会丢失

    //searchLiveData 中的查询条件（query）改变时，
    // 会调用 Repository.searchPlaces(query) 方法获取新的 LiveData<Result<List<Place>>> 对象，并将其赋值给 placeLiveData
    val placeLiveData: LiveData<Result<List<Place>>> = searchLiveData.switchMap { query ->
        //调用仓库层中定义的searchPlaces()方法就可以发起网络请求,将仓库层返回的LiveData对象转换成一个可供Activity观察的LiveData对象
        Repository.searchPlaces(query)
    }

    fun searchPlaces(query: String) {
        //将传入的搜索参数赋值给了一个searchLiveData对象,每当searchPlaces()函数被调用时,switchMap()方法所对应的转换函数就会执行
        searchLiveData.value = query
    }
}