package com.example.tmartin.bluetooth_duino

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_led_control.*
import java.io.IOException
import java.io.InputStream
import java.util.*


class LedControl : AppCompatActivity() {

    private var mInStream: InputStream? = null

    companion object {
        var mMyUUID: UUID = UUID.fromString("1f11bed2-9324-4f65-9069-0817ec5ca663")
        var mBluetoothSocket: BluetoothSocket? = null
        lateinit var mProgressDial: ProgressDialog
        lateinit var mBluetoothAdapter: BluetoothAdapter
        var mIsConnected: Boolean = false
        lateinit var mAddress: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_led_control)

        val _layout = findViewById<ConstraintLayout>(R.id.layout)
        val background = CreateCanvas(this)
        _layout.addView(background)

        control_forward.setOnClickListener { sendCommand("f") }
        control_left.setOnClickListener {
            //sendCommand("l")
            val rotate = Rotate(this)
            _layout.addView(rotate)
        }
        control_right.setOnClickListener { sendCommand("r") }
        control_back.setOnClickListener { sendCommand("b") }
        control_led_disconnect.setOnClickListener { disconnect() }

/*
        mAddress = intent.getStringExtra(DeviceListActivity.EXTRA_ADDRESS)
        ConnectToDevice(this).execute()

*/

        for (i in 1..50)
        {
            val bgrnd = CreatePoints(this)
            _layout.addView(bgrnd)
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

            if(Math.sqrt(Math.pow(randX-centerX, 2.toDouble()) + Math.pow(randY-centerY, 2.toDouble())) <= 340)
            {
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
        }
    }


    private fun sendCommand(input: String) {
        Log.d("LedControl", "sendCommand, inpu: ${input}")
        if (mBluetoothSocket != null)
            try {
                mBluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
    }

    private fun disconnect() {
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket!!.close()
                mBluetoothSocket = null
                mIsConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            mProgressDial = ProgressDialog.show(context, "Connecting...", "Please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (mBluetoothSocket == null || !mIsConnected) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = mBluetoothAdapter.getRemoteDevice(mAddress)
                    mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(mMyUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    mBluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                mIsConnected = true
            }
            mProgressDial.dismiss()
        }

    }
}