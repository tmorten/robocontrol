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
import android.graphics.Point
import android.graphics.drawable.RotateDrawable
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_led_control.*
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask


class LedControl : AppCompatActivity() {
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
        val _layout = findViewById(R.id.layout) as android.support.constraint.ConstraintLayout
        val background = CreateCanvas(this)
        _layout.addView(background)
        /*
        mAddress = intent.getStringExtra(DeviceListActivity.EXTRA_ADDRESS)
        ConnectToDevice(this).execute()*/

        control_forward.setOnClickListener { sendCommand("f") }
        control_left.setOnClickListener {
            /*sendCommand("l")
            val rotate = RotateRight(this)
            _layout.addView(rotate)*/
            _layout.getChildAt(6).rotation -= (20).toFloat()
        }
        control_right.setOnClickListener {
            //sendCommand("r")
            _layout.getChildAt(6).rotation += (20).toFloat()
        }
        control_back.setOnClickListener { sendCommand("b") }
        control_led_disconnect.setOnClickListener { disconnect() }

        val bgrnd = CreatePoints(this)
        _layout.addView(bgrnd)
    }
    fun rand(s: Int, e: Int) = Random().nextInt(e + 1 - s) + s


    class RotateRight(context: Context) : View(context)
    {
        override fun onDraw(canvas: Canvas?) {
            canvas?.rotate((180).toFloat())
        }
    }

    class CreatePoints(context: Context) : View(context)
    {
        val circles:List<Point>? = null
        override fun onDraw(canvas: Canvas?) {
            val brush = Paint()
            brush.setARGB(255, 255,0,0)

            val randx = rand(0, 720)
            val randy = rand(0, 1118)

            val centerx = (720/2).toDouble()
            val centery = (1118 - (1118/3)).toDouble()

            canvas?.drawCircle(centerx.toFloat(), (centery-30).toFloat(), 5.toFloat(), brush)
            /*if(Math.sqrt(Math.pow(randx-centerx, 2.toDouble()) + Math.pow(randy-centery, 2.toDouble())) <= 340)
            {

            }*/

        }

        fun rand(s: Int, e: Int) = Random().nextInt(e + 1 - s) + s
    }

    class CreateCanvas (context: Context): View(context) {
        var myCanvas:Canvas? = null
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
            myCanvas = canvas
            /*
            for (i in 1..10)
            {
                val centerx = (width/2).toDouble()
                val centery = (height - (height/3)).toDouble()

                val randx = rand(0, width)
                val randy = rand(0, height)

                val brush = Paint()
                brush.setARGB(255, 255,0,0)
                if(Math.sqrt(Math.pow(randx-centerx, 2.toDouble()) + Math.pow(randy-centery, 2.toDouble())) <= 340)
                {
                    canvas?.drawCircle((randx).toFloat(), (randy).toFloat(), 5.toFloat(), brush)

                }
            }*/
        }

        fun getCanvas(): Canvas?
        {
            return myCanvas
        }

        fun rand(s: Int, e: Int) = Random().nextInt(e + 1 - s) + s
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