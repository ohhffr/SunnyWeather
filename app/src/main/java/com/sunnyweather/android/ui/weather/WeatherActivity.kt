package com.sunnyweather.android.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

class WeatherActivity : AppCompatActivity() {
    val weatherViewModel by lazy { ViewModelProvider(this)[WeatherViewModel::class.java] }
    private lateinit var placeName: TextView
    private lateinit var currentTemp: TextView
    private lateinit var currentSky: TextView
    private lateinit var currentAQI: TextView
    private lateinit var nowLayout: RelativeLayout
    private lateinit var forecastLayout: LinearLayout
    private lateinit var coldRiskText: TextView
    private lateinit var dressingText: TextView
    private lateinit var ultravioletText: TextView
    private lateinit var carWashingText: TextView
    private lateinit var weatherLayout: ScrollView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var gestureDetector: GestureDetector

    private lateinit var navBtn: Button
    lateinit var drawerLayout: DrawerLayout


    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置窗口属性，使内容延伸到状态栏下方
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_weather)
        placeName = findViewById(R.id.placeName)
        currentTemp = findViewById(R.id.currentTemp)
        currentSky = findViewById(R.id.currentSky)
        currentAQI = findViewById(R.id.currentAQI)
        nowLayout = findViewById(R.id.nowLayout)
        forecastLayout = findViewById(R.id.forecast_layout)
        coldRiskText = findViewById(R.id.coldRiskText)
        dressingText = findViewById(R.id.dressingText)
        ultravioletText = findViewById(R.id.ultravioletText)
        carWashingText = findViewById(R.id.carWashingText)
        weatherLayout = findViewById(R.id.weatherLayout)
        swipeRefreshLayout = findViewById(R.id.swipeRefresh)

        navBtn = findViewById(R.id.navBtn)
        drawerLayout = findViewById(R.id.drawerLayout)


        //从Intent中取出经纬度坐标和地区名称,赋值到WeatherViewModel的相应变量中
        if (weatherViewModel.locationLng.isEmpty()) {
            weatherViewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (weatherViewModel.locationLat.isEmpty()) {
            weatherViewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (weatherViewModel.placeName.isEmpty()) {
            weatherViewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        //weatherLiveData对象进行观察
        weatherViewModel.weatherLiveData.observe(this) { result ->
            val weather = result.getOrNull()
            if (weather != null) { //当获取到服务器返回的天气数据时
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            swipeRefreshLayout.isRefreshing = false
        }

        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary)//设置下拉刷新进度条的颜色
        refreshWeather()//刷新天气信息
        swipeRefreshLayout.setOnRefreshListener {//下拉刷新的监听器
            refreshWeather()
        }

        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)//打开滑动菜单
        }
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerStateChanged(newState: Int) {}

        })
        setupGestureDetector()
        setupTouchListener()
    }

    fun refreshWeather() {
        weatherViewModel.refreshWeather(weatherViewModel.locationLng, weatherViewModel.locationLat)//执行一次刷新天气的请求
        swipeRefreshLayout.isRefreshing = true
    }

    @SuppressLint("SetTextI18n")
    private fun showWeatherInfo(weather: Weather) {//从Weather对象中获取数据，然后显示到相应的控件上
        placeName.text = weatherViewModel.placeName

        val realtime = weather.realtime
        val daily = weather.daily
        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        currentTemp.text = currentTempText

        currentSky.text = getSky(realtime.skycon).info

        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text = currentPM25Text

        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml布局中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {//未来几天天气预报的部分,处理每天的天气信息
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]

            val view = layoutInflater.inflate(R.layout.forecast_item, forecastLayout, false)
            val dateInfo = view.findViewById<TextView>(R.id.dateInfo)
            val skyIcon = view.findViewById<ImageView>(R.id.skyIcon)
            val temperatureInfo = view.findViewById<TextView>(R.id.temperatureInfo)

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sky = getSky(skycon.value)

            dateInfo.text = simpleDateFormat.format(skycon.date)
            skyIcon.setImageResource(sky.icon)
            temperatureInfo.text = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            forecastLayout.addView(view)
        }

        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout.visibility = View.VISIBLE//让ScrollView变成可见状态
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                // 判断右滑手势：水平滑动距离 > 垂直滑动距离，且右滑距离超过 100px
                val deltaX = e2.x - (e1?.x ?: 0f)
                val deltaY = e2.y - (e1?.y ?: 0f)
                if (abs(deltaX) > abs(deltaY) && deltaX > 100) {
                    drawerLayout.openDrawer(GravityCompat.START)
                    return true
                }
                return false
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        // 在天气内容区域（ScrollView 或其父布局）添加触摸监听
        weatherLayout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false // 返回 false 确保不拦截正常滚动事件
        }
    }

}