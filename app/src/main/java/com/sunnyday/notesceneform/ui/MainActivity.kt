package com.sunnyday.notesceneform.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sunnyday.notesceneform.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        helloSceneForm.setOnClickListener {
            startActivity(Intent(this,HelloSceneFormActivity::class.java))
        }
    }
}