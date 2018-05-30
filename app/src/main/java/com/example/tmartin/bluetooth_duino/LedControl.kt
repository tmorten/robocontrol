package com.example.tmartin.bluetooth_duino

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.support.constraint.ConstraintLayout
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
    lateinit var layout : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_led_control)
        layout = findViewById<ConstraintLayout>(R.id.layout)
        val background = CreateCanvas(this)
        (layout as ConstraintLayout?)?.addView(background)

        m_BlueT = BluetoothSPP(this)

        if(!m_BlueT.isBluetoothAvailable()) {
            // bluetooth is not available
            Toast.makeText(applicationContext, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            finish()
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
    }

    override fun onResume() {
        super.onResume()
        m_BlueT.setOnDataReceivedListener { data, message ->
            try {
                val canvas = CreateCanvas(this)
                (layout as ConstraintLayout).addView(canvas)
                var mDistance = message.split('-')[0].toInt()
                var mAngle = message.split('-')[1].toInt()
                Log.d("RoboDuino", "Angle: ${mAngle}, Distance: ${mDistance}")

                val point = CreatePoints(this, mAngle, mDistance)
                (layout as ConstraintLayout).addView(point)
                }
            catch (e: Exception){

            }
        }
    }

    class  Rotate(context: Context) : View(context)
    {
        override fun onDraw(canvas: Canvas?) {
            canvas?.rotate(20.toFloat())
        }
    }

    class CreatePoints(context: Context, private val mAngle: Int, private val mDistance: Int) : View(context){
        override fun onDraw(canvas: Canvas?) {
            val brush = Paint()
            brush.setARGB(255, 255, 0, 0)
            val centerX = canvas!!.width/2
            val centerY = canvas!!.height - canvas.height/3
            val convDistance = mDistance / 50.0f * (width/2 - 30)

            canvas!!.drawCircle(GetNewXPoint(mAngle, centerX, centerY, convDistance), GetNewYPoint(mAngle, centerX, centerY, convDistance), 15.toFloat(), brush)
        }

        fun GetNewXPoint(angle : Int, centerX : Int, centerY: Int, distance : Float) : Float
        {
            val angleRad = angle * Math.PI/180
            val cosTheta = Math.cos(angleRad)
            val sinTheta = Math.sin(angleRad)
            return (cosTheta * (centerX - centerX) - sinTheta * (centerY-distance - centerY) + centerX).toFloat()
        }

        fun GetNewYPoint(angle : Int, centerX: Int, centerY : Int, distance: Float) : Float
        {
            val angleRad = angle * Math.PI/180
            val cosTheta = Math.cos(angleRad)
            val sinTheta = Math.sin(angleRad)
            return (sinTheta * (centerX - centerX) + cosTheta * (centerY-distance - centerY) + centerY).toFloat()
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
            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(), (width-30)/2.toFloat(), brushRadar)
            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(), (width-30)/4.toFloat(), brushRadar)
            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(), (width-30)/8.toFloat(), brushRadar)
            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(), (width-30)/16.toFloat(), brushRadar)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!m_BlueT.isBluetoothEnabled) {
            // BT is disabled
            m_BlueT.enable()
        } else {
            // BT is enabled
            if (!m_BlueT.isServiceAvailable) {
                m_BlueT.setupService()
                m_BlueT.startService(BluetoothState.DEVICE_OTHER)
                setup()
            }
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

    private fun setup() {
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

        control_stop.setOnClickListener {
            Log.d("RoboDuino", "Stop button pushed")
            m_BlueT.send("s", true)
        }
    }
}