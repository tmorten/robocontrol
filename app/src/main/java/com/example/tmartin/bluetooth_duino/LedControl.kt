package com.example.tmartin.bluetooth_duino

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.View
import android.widget.Toast
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import kotlinx.android.synthetic.main.activity_led_control.*
import java.util.*


class LedControl : AppCompatActivity() {

    lateinit var m_BlueT : BluetoothSPP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_led_control)

        m_BlueT = BluetoothSPP(this)

        if(!m_BlueT.isBluetoothAvailable()) {
            // bluetooth is not available
            Toast.makeText(applicationContext, "Bluetooth is not available", Toast.LENGTH_LONG).show()
        }

        m_BlueT.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String?, address: String?) {
                control_led_connect.text = "Connected to ${name}"
            }

            override fun onDeviceDisconnected() {
                control_led_connect.text = "Connection lost"
            }

            override fun onDeviceConnectionFailed() {
                control_led_connect.text = "Unable to connet"
            }
        })

        control_led_connect.setOnClickListener {
            if (m_BlueT.serviceState == BluetoothState.STATE_CONNECTED) {
                m_BlueT.disconnect()
            } else {
                val intent = Intent(applicationContext, DeviceList::class.java)
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
            }
        }

        control_forward.setOnClickListener {
            Log.d("RoboDuino", "Forward button pushed")
            m_BlueT.send("f", true)
        }

        control_left.setOnClickListener {
            Log.d("RoboDuino", "Left button pushed")
            m_BlueT.send("l", true)
        }

        control_right.setOnClickListener {
            Log.d("RoboDuino", "Right button pushed")
            m_BlueT.send("r", true)
        }

        control_back.setOnClickListener {
            Log.d("RoboDuino", "Backward button pushed")
            m_BlueT.send("b", true)
        }

        for (i in 1..50)
        {
            val bgrnd = CreatePoints(this)
            layout.addView(bgrnd)
        }
    }

    class  Rotate(context: Context) : View(context)
    {
        override fun onDraw(canvas: Canvas?) {
            canvas?.rotate(20.toFloat())
        }
    }

    class CreatePoints(context: Context) : View(context){
        override fun onDraw(canvas: Canvas?) {
            val brush = Paint()
            brush.setARGB(255, 255, 0, 0)
            val centerX = 360
            val centerY = 1118-1118/3
            val randX = random(0, 720).toDouble()
            val randY = random(0, 1118).toDouble()
            val distance = Math.sqrt(Math.pow(randX-centerX, 2.toDouble()) + Math.pow(randY-centerY, 2.toDouble()))

            if( distance <= 340)
            {
                if (distance < 44.5)
                    brush.setARGB(255, 255, 0, 0)
                else if (distance > 44.5 && distance < 85)
                    brush.setARGB(255, 64, 128, 64)
                else if (distance > 85 && distance < 170)
                    brush.setARGB(255, 0, 255, 0)
                else if (distance > 170 && distance < 255)
                    brush.setARGB(255, 64, 64, 128)
                else
                    brush.setARGB(255, 0, 0, 255)
                canvas?.drawCircle(randX.toFloat(), randY.toFloat(), 5.toFloat(), brush)
            }
        }

        fun random(from: Int, to: Int) : Int {
            return Random().nextInt(to - from) + from
        }
    }

    class CreateCanvas (context: Context): View(context) {

        override fun onDraw (canvas: Canvas) {
            canvas.drawRGB (255, 255, 255)
            val width = getWidth ()
            val height = getHeight ()
            val brushRadar = Paint ()
            val brushCenter = Paint()
            brushRadar.setARGB (255, 0, 0, 0)
            brushCenter.setARGB(255, 0, 0, 0)
            brushRadar.setStyle (Paint.Style.STROKE)

            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(),5.toFloat(), brushCenter)
            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(), 340.toFloat(), brushRadar)
            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(), 255.toFloat(), brushRadar)
            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(), 170.toFloat(), brushRadar)
            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(), 85.toFloat(), brushRadar)
            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(), (44.5).toFloat(), brushRadar)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!m_BlueT.isBluetoothEnabled) {
            // BT is disabled
            m_BlueT.enable()
        } else {
            // BT is enabled
            m_BlueT.setupService()
            m_BlueT.startService(BluetoothState.DEVICE_OTHER)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        m_BlueT.stopService()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                m_BlueT.connect(data)
            } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
                if (resultCode == Activity.RESULT_OK) {
                    m_BlueT.setupService()
                } else {
                    Toast.makeText(applicationContext, "Bluetooth was not enabled", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}