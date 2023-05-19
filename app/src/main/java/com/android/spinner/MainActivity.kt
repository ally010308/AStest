package com.android.spinner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var spinner : Spinner
    lateinit var result : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) //레이아웃 적용

        spinner = findViewById(R.id.spinner)
        result = findViewById(R.id.result)

        spinner.onItemSelectedListener = this

        var arrayAdapter = ArrayAdapter.createFromResource(
            applicationContext,
            R.array.stations,
            android.R.layout.simple_spinner_item
        )
//
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter

        //검색가능 스피너 부분
//        val items = arrayOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
//
        //val autoCompleteTextView: AutoCompleteTextView = findViewById(R.id.search_Spinner)
//        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
//        autoCompleteTextView.setAdapter(adapter)
        //



    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        result.text = parent!!.getItemAtPosition(position).toString()

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    fun onButton1Clicked(v:View) { //버튼 클릭시 실행

        val destination = result.text

        Toast.makeText(this, "안내를 시작합니다", Toast.LENGTH_LONG).show()

        val intent = Intent(this, SubActivity::class.java) //클래스 이름

        intent.putExtra("destination", destination)//데이터 전달

        startActivity(intent)
    }
}

