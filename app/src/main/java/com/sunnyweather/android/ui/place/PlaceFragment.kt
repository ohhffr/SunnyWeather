package com.sunnyweather.android.ui.place

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceFragment : Fragment() {
    val placeViewModel by lazy { ViewModelProvider(this)[PlaceViewModel::class.java] }

    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_place, container,false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val searchEdit = view.findViewById<EditText>(R.id.searchEditText)
        val bgImageView = view.findViewById<ImageView>(R.id.bgImageView)

        //设置布局管理器
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        //设置适配器
        adapter = PlaceAdapter(this,placeViewModel.placeList)
        recyclerView.adapter = adapter

        searchEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()){
                placeViewModel.searchPlaces(content)
            }else{
                recyclerView.visibility = View.GONE
                bgImageView.visibility = View.VISIBLE
                placeViewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }

        //viewLifecycleOwner：这是 Fragment 从 AndroidX 库开始提供的一个属性，它代表了 Fragment 视图的生命周期。
        // 使用 viewLifecycleOwner 作为 LifecycleOwner，当 Fragment 的视图被销毁时，LiveData 的观察操作会自动停止，避免了潜在的 UI 更新问题。
        placeViewModel.placeLiveData.observe(viewLifecycleOwner) { result ->//当有任何数据变化时,会回调到传入的Observer接口实现中
          result.getOrNull()?.let {  place ->
            //如果数据不为空，那么就将这些数据添加到PlaceViewModel的placeList集合中，并通知PlaceAdapter刷新界面
                recyclerView.visibility = View.VISIBLE
                bgImageView.visibility = View.GONE
                placeViewModel.placeList.clear()
                placeViewModel.placeList.addAll(place)
                adapter.notifyDataSetChanged()
          } ?: run {
              //如果数据为空，则说明发生了异常
              //由于在 Fragment 中使用 Toast 需要一个 Context 对象，而 activity 属性可能为 null，
              // 因此使用 requireActivity() 来确保获取到有效的 Activity 实例作为 Context
              Toast.makeText(requireActivity(), "未查询到任何地点", Toast.LENGTH_SHORT).show()
              result.exceptionOrNull()?.printStackTrace()
          }
        }

        // 检查是否已有保存的地点
        placeViewModel.checkIsPlaceSaved()
        // 观察是否已保存地点
        placeViewModel.isPlaceSavedResultLiveData.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { isSaved ->
                if (isSaved) {
                    // 如果已保存，获取地点信息
                    placeViewModel.getSavedPlace()
                } else {
                    recyclerView.visibility = View.GONE
                    bgImageView.visibility = View.VISIBLE
                }
            } ?: run {
                // 处理异常
                Toast.makeText(context, "检查失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 观察保存的地点数据
        placeViewModel.getSavedPlaceResultLiveData.observe(viewLifecycleOwner) { result ->
            result.getOrNull()?.let { place ->
                // 跳转到天气界面
                Intent(context, WeatherActivity::class.java).apply {
                    putExtra("location_lng", place.location.lng)
                    putExtra("location_lat", place.location.lat)
                    putExtra("place_name", place.name)
                    startActivity(this)
                }
            } ?: run {
                Toast.makeText(context, "获取失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

}