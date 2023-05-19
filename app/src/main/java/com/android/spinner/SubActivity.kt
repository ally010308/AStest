package com.android.spinner

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
//import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.core.content.ContextCompat
//import com.android.volley.Request
//import com.devjaewoo.sensormonitoringapp.databinding.ActivityMainBinding
//import com.devjaewoo.sensormonitoringapp.request.RequestHandler

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import android.graphics.Color // 그래프 그리기
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
////// RecycleView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
////

class SubActivity : AppCompatActivity(), SensorEventListener {

    private var mSensorManager: SensorManager? = null
    //mp 그래프
    private lateinit var lineChart: LineChart
    private var entries: ArrayList<Entry> = ArrayList()
    private var handler: Handler = Handler()

    //recycleView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private val data: MutableList<String> = mutableListOf()
    //recycleView

//    private var handler2 : Handler


    // sensors
    private var mSensorLinearAcceleration: Sensor? = null



    private var tvLineX: TextView? = null
    private var tvLineY: TextView? = null
    private var tvLineZ: TextView? = null

    private var tvValacc: TextView? = null  // 가속도 크기
    private var tvcase: TextView? = null    // 지하철 case

    private var tvSpeedX: TextView? = null
    private var tvDisX: TextView? = null

    private var tvSpeedY: TextView? = null
    private var tvDisY: TextView? = null

    private var tvSpeedZ: TextView? = null
    private var tvDisZ: TextView? = null

    private var tvTotD: TextView? = null

    private var ValAcc = 0F
    private var case : Int = 0
    private val starttime1 = 0f
    private val endtime1 = 0f
    private val endtime2 = 0f
    private val endtime3 = 0f

    // Sensor's values
    private var line = FloatArray(3)

    private var nowAccX = 0F  //Float 타입임
    private var recentSpeedX:Float = 0F //A
    private var nowSpeedX:Float = 0F  //B
    private var nowDistanceX:Float = 0F
    private var distanceX:Float = 0F //이동거리

    private var nowAccY = 0F  //Float 타입임
    private var recentSpeedY:Float = 0F //A
    private var nowSpeedY:Float = 0F  //B
    private var nowDistanceY:Float = 0F
    private var distanceY:Float = 0F //이동거리

    private var nowAccZ = 0F  //Float 타입임
    private var recentSpeedZ:Float = 0F //A
    private var nowSpeedZ:Float = 0F  //B
//    private var nowDistanceZ:Float = 0F
    private var distanceZ:Float = 0F //이동거리


    private var totalD:Float = 0F //이동거리
    private var totalSpeed = 0F             // 추가
    private var last_TotalSp = 0F

    private var betweenStationDis:Float = 0F // 이동거리가 저장되는 곳 ?


    //시간계산
    private var previousTime:Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        ////// recycleView 설정 부분  /////***************
        recyclerView = findViewById(R.id.recycleView)
        adapter = MyAdapter(data) // 데이터를 가져오는 함수에 맞게 수정 필요

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        ////// recycleView 설정 부분  /////****************

        // Identify the sensors that are on a device
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        //mp 그래프
        lineChart = findViewById(R.id.chart)
        lineChart.setNoDataText("No data available")
        // 그래프 스타일 설정

        //mp 그래프
        // Assign the textViews
        tvLineX = findViewById<View>(R.id.label_lineX) as TextView
        tvLineY = findViewById<View>(R.id.label_lineY) as TextView
        tvLineZ = findViewById<View>(R.id.label_lineZ) as TextView
        tvValacc = findViewById<View>(R.id.label_totalacc) as TextView
        tvcase = findViewById<View>(R.id.label_case) as TextView

        tvSpeedX = findViewById<View>(R.id.label_speedX) as TextView
        tvDisX = findViewById<View>(R.id.label_disX) as TextView

        tvSpeedY = findViewById<View>(R.id.label_speedY) as TextView
        tvDisY = findViewById<View>(R.id.label_disY) as TextView

        tvSpeedZ = findViewById<View>(R.id.label_speedZ) as TextView
        tvDisZ = findViewById<View>(R.id.label_disZ) as TextView

        tvTotD = findViewById<View>(R.id.label_totalDis) as TextView

        // sensors connection
        mSensorLinearAcceleration = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        // Check if all sensors are available
        val sensor_error = resources.getString(R.string.error_no_sensor)

        if (mSensorLinearAcceleration == null) {
            tvLineX!!.text = sensor_error
            tvLineY!!.text = sensor_error
            tvLineZ!!.text = sensor_error

            tvSpeedX!!.text = sensor_error
            tvDisX!!.text = sensor_error

            tvSpeedY!!.text = sensor_error
            tvDisY!!.text = sensor_error

            tvSpeedZ!!.text = sensor_error
            tvDisZ!!.text = sensor_error

            tvTotD!!.text = sensor_error
        }

        val stopButton: Button = findViewById(R.id.stop_button)
        stopButton.setOnClickListener {
            // 버튼을 클릭했을 때 실행되어야 하는 동작을 여기에 작성
            reset()
            Toast.makeText(this, "이동이 멈춤", Toast.LENGTH_LONG).show()
        }
    }
    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this, mSensorLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL)
        startDataUpdate()
    }

    override fun onStart() {
        super.onStart()
        if (mSensorLinearAcceleration != null) { mSensorManager!!.registerListener(this, mSensorLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL) }

//        handler2.post(handlerTask)
    }
    override fun onPause() {  // onStop 을 onPause 로 대체 mp 그래프
        super.onPause()
        // Stop listening the sensors
        mSensorManager!!.unregisterListener(this)
        stopDataUpdate()   // mp 그래프
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Get sensors data when values changed
        val sensorType = event.sensor.type
        when (sensorType) {

            Sensor.TYPE_LINEAR_ACCELERATION -> {
                line = event.values
                tvLineX!!.text = resources.getString(R.string.label_lineX, line[0])
                tvLineY!!.text = resources.getString(R.string.label_lineY, line[1])
                tvLineZ!!.text = resources.getString(R.string.label_lineZ, line[2])
                val totalAcc =sqrt((line[0].pow(2) + line[1].pow(2))+ line[2].pow(2).toDouble()).toFloat()
                ValAcc =  String.format("%.2f", totalAcc).toFloat()
                tvValacc!!.text = "Total Acceleration : $ValAcc"

                entries.add(Entry(entries.size.toFloat(), ValAcc)) // mp그래프

                nowAccX = round(line[0]*100)/100  //소수점 두번째 까지 끊음
                nowAccY = round(line[1]*100)/100
                nowAccZ = round(line[2]*100)/100

                getDiswT()
                when (case) {
                    0 -> if(ValAcc > 1f){ //서서히 속도가 증가하는 중
                        val starttime1 = System.currentTimeMillis()
                        case++
                        }
                    1 -> if(ValAcc < 0.1f){ // 일정속도로 이동중
                        val endtime1 = System.currentTimeMillis()
                        val time1 = (endtime1 - starttime1) / 1000.0
                        case++
                        }
                    2 -> if(ValAcc >1f){    //서서히 속도가 증가하는 중
                        val endtime2 = System.currentTimeMillis()
                        val time2 = (endtime2 - endtime1) / 1000.0
                        case++
                    }
                    3 -> if(ValAcc < 0.01f) { //멈춤
                        val endtime3 = System.currentTimeMillis()
                        val time3 = (endtime3 - endtime2) / 1000.0
                        case = 0
//                        performCallback()
                        // 이동거리 계산 + 역간거리 비교해서 출발역 설정 부분 추가

                    }

                    else -> println("Value is neither 1 nor 2 nor 3")
                }

                tvcase?.text = "Moving case : $case"

//                testStop() //

//                nowAccX = round(line[0]*100)/100
//                nowAccY = round(line[1]*100)/100
//                nowAccZ = round(line[2]*100)/100

                // 가속도 센서 값이 변할 때마다 호출되는 콜백 함수
//                handleAcceleration(line[0], line[1], line[2]) { distance ->
//                    // 콜백 함수에서 반환된 이동 거리 처리
//                    // ...
//                }
//                ValAcc =  sqrt((line[0].pow(2) + line[1].pow(2))+ line[2].pow(2).toDouble()).toFloat()
            }
            else -> { }
        }

    }

    private fun startDataUpdate() {  // mp 그래프 함수
        handler.post(object : Runnable {
            override fun run() {

                    updateChart()
                    handler.postDelayed(this, 100) // 100ms마다 업데이트


            }
        })
    }

    private fun updateChart() {
        val dataSet = LineDataSet(entries, "Acceleration")
        dataSet.color = Color.BLUE
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)

        val lineDataSets: ArrayList<ILineDataSet> = ArrayList()
        lineDataSets.add(dataSet)

        val lineData = LineData(lineDataSets)

        lineChart.data = lineData
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    private fun stopDataUpdate() {      //mp 그래프 함수
        handler.removeCallbacksAndMessages(null)
    }
//    private fun getDistanceX() {
//        nowSpeedX = recentSpeedX+(nowAccX/100)
//        nowDistanceX = ((nowSpeedX+recentSpeedX)/2)/100
//        distanceX += nowDistanceX
//        recentSpeedX = nowSpeedX
//
//        nowSpeedY = recentSpeedY+(nowAccY/100)
//        nowDistanceY = ((nowSpeedY+recentSpeedY)/2)/100
//        distanceY += nowDistanceY
//        recentSpeedY = nowSpeedY
//
//        nowSpeedZ = recentSpeedZ+(nowAccZ/100)
//        nowDistanceZ = ((nowSpeedZ+recentSpeedZ)/2)/100
//        distanceZ += nowDistanceZ
//        recentSpeedZ = nowSpeedZ
//
//        totalD += sqrt((nowDistanceX).pow(2)+(nowDistanceY).pow(2)+(nowDistanceZ).pow(2))
//
//    }


   // val handler = Handler()
//    val handler2 = Handler()

    val millisTime = 10  //1000=1초에 한번씩 실행
//    private val handlerTask = object : Runnable {
//        override fun run() {
//
//                // handler1과의 동기화
//                // 작업 수행
//                getDistanceX()
//                tvSpeedX!!.text = resources.getString(R.string.label_speedX, nowSpeedX)
//                tvDisX!!.text = resources.getString(R.string.label_disX, distanceX)
//
//                tvSpeedY!!.text = resources.getString(R.string.label_speedY, nowSpeedY)
//                tvDisY!!.text = resources.getString(R.string.label_disY, distanceY)
//
//                tvSpeedZ!!.text = resources.getString(R.string.label_speedZ, nowSpeedZ)
//                tvDisZ!!.text = resources.getString(R.string.label_disZ, distanceZ)
//
//
//                tvTotD!!.text = resources.getString(R.string.label_totalDis, totalD)
//
////                handler2.postDelayed(this, millisTime.toLong()) // millisTiem 이후 다시
//            }
//
//        }
//    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    /*
    fun computeOrientation() {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(rotationMatrix, null, acc, mag)

        val orientationAngles = FloatArray(3)
        var radian = SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // Convert angles from radians to degree
        val angles = FloatArray(3)
        angles[0] = (radian[0].toDouble() * 180 / 3.14).toFloat()
        angles[1] = (radian[1].toDouble() * 180 / 3.14).toFloat()
        angles[2] = (radian[2].toDouble() * 180 / 3.14).toFloat()

        tvAzimuth!!.text = resources.getString(R.string.label_azimuth, angles[0])
        tvPitch!!.text = resources.getString(R.string.label_pitch, angles[1])
        tvRoll!!.text = resources.getString(R.string.label_roll, angles[2])
    }
     */


// 가속도 값 처리 및 결과를 반환하는 콜백 함수
//fun handleAcceleration(x: Float, y: Float, z: Float, callback: (Float) -> Unit) {
//    // 가속도 값 처리
//    val acceleration = calculateAcceleration(x, y, z)
//
//    // 이동 거리 계산
//    val distance = calculateDistance(acceleration)
//
//    // 결과를 콜백 함수에 전달
//    callback(distance)
//}

// 가속도 값에 대한 처리 함수
//fun calculateAcceleration(x: Float, y: Float, z: Float): Float {
//    // 가속도 값 처리 로직
//    var accelerationValue = 0F
//    accelerationValue =  sqrt((line[0].pow(2) + line[1].pow(2))+ line[2].pow(2).toDouble()).toFloat()
//    // ...
//
//    return accelerationValue
//}

// 이동 거리 계산 함수
//fun calculateDistance(acceleration: Float): Float {
//    // 이동 거리 계산 로직
//    // ...
//    var subcase = 0F
//    if(acceleration > 0.1){
//        subcase++
//    }
//    return distanceValue
//    }

    fun performCallback(start1:Float, end1:Float, end2:Float, end3:Float) {
        // 콜백 함수 내용 작성
        // 이곳에서 필요한 작업 수행
    }

    private fun reset() {
//        tvMove!!.text = resources.getString(R.string.stop)
        nowAccX = 0F
        nowAccY = 0f
        nowAccZ = 0f
        recentSpeedX = 0f
        recentSpeedY = 0f
        recentSpeedZ = 0f


        //현재까지 이동거리 저장

        data.add(betweenStationDis.toString())

        // 어댑터에 변경 사항 알림
        adapter.notifyDataSetChanged()

        betweenStationDis=0f
        /*
            val adapter = listView.adapter as ArrayAdapter<String>(applicationContext, android.R.layout.simple_list_item_1, items)
            adapter.add(betweenStationDis.toString())
            adapter.notifyDataSetChanged()

             */
    }

    private fun getDiswT() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = (currentTime - previousTime) / 1000.0

        //X
        nowSpeedX = recentSpeedX+nowAccX*(elapsedTime.toFloat())
        val avgSpeedX = (nowSpeedX+recentSpeedX) / 2
        distanceX = avgSpeedX*elapsedTime.toFloat()

        //Y
        nowSpeedY = recentSpeedY+nowAccY*elapsedTime.toFloat()
        val avgSpeedY = (nowSpeedY+recentSpeedY) / 2
        distanceY = avgSpeedY*elapsedTime.toFloat()

        //Z
        nowSpeedZ = recentSpeedZ+nowAccZ*elapsedTime.toFloat()
        val avgSpeedZ = (nowSpeedZ+recentSpeedZ) / 2
        distanceZ = avgSpeedZ*elapsedTime.toFloat()


        //총 거리
        totalD += sqrt((distanceX).pow(2)+(distanceY).pow(2)+(distanceZ).pow(2))

        //역간거리
        betweenStationDis+= sqrt((distanceX).pow(2)+(distanceY).pow(2)+(distanceZ).pow(2) )

        //이월
        recentSpeedX = nowSpeedX
        recentSpeedY = nowSpeedY
        recentSpeedZ = nowSpeedZ
        previousTime = currentTime

    }
//    private fun testStop() {
//
//        val x = nowSpeedX
//        val y = nowSpeedY
//        val z = nowSpeedZ
//
//        last_TotalSp = totalSpeed
//        totalSpeed = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
////        val deltaSpeed = totalSpeed - last_TotalSp
//
////        if (deltaSpeed > 1.5f) {
////            // 이동 감지
////            tvMove!!.text = resources.getString(R.string.moving)
////            isMoving = true
////        } else if (isMoving && totalSpeed < 10) {
////            Toast.makeText(this, "이동이 멈춤", Toast.LENGTH_LONG).show()
////
////            isMoving = false
//            //stopCount++
//            //tvCount!!.text = resources.getString(R.string.label_count, stopCount)
////            reset()
//        }
    private fun getData(): List<String> {   // recycleView
        return listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
    }


}


