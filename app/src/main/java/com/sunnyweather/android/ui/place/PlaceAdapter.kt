package com.sunnyweather.android.ui.place

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceAdapter(private val fragment: PlaceFragment, private val placeList: List<Place>) :
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {
        inner class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val placeName: TextView = view.findViewById(R.id.place_name)
            val placeAddress: TextView = view.findViewById(R.id.place_address)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent, false)
        val holder = PlaceViewHolder(view)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val place = placeList[position]
            val activity = fragment.activity

            // 隐藏键盘
            val inputMethodManager = fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val searchEditText = fragment.view?.findViewById<EditText>(R.id.searchEditText) // 替换为你的搜索框ID

            searchEditText?.let { editText ->
                inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
                editText.clearFocus() // 清除焦点
            }

            if (activity is WeatherActivity){//如果是在WeatherActivity中
                activity.drawerLayout.closeDrawers()//关闭滑动菜单
                //给WeatherViewModel赋值新的经纬度坐标和地区名称
                activity.weatherViewModel.locationLng = place.location.lng
                activity.weatherViewModel.locationLat = place.location.lat
                activity.weatherViewModel.placeName = place.name
                activity.refreshWeather()//刷新城市的天气信息
            }else{//如果是在MainActivity中
                val intent = Intent(parent.context, WeatherActivity::class.java).apply {
                    putExtra("location_lng", place.location.lng)
                    putExtra("location_lat", place.location.lat)
                    putExtra("place_name", place.name)
                }
                fragment.startActivity(intent)
                // 结束 MainActivity，避免返回时回到主界面
                fragment.activity?.finish()
            }

            fragment.placeViewModel.savePlace(place)
            // 如果需要在 Adapter 中进行额外的操作，也可以从 ViewModel 中观察保存的结果
            fragment.placeViewModel.savePlaceResultLiveData.observe(fragment.viewLifecycleOwner) { result ->
                val savedPlace = result.getOrNull()
                if (savedPlace == null) {
                    Toast.makeText(parent.context, "保存失败", Toast.LENGTH_SHORT).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
            }

        }
        return holder
    }

    override fun getItemCount() = placeList.size

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placeList[position]
        holder.placeName.text = place.name
        holder.placeAddress.text = place.address
    }
}