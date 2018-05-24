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

        val layout1 = findViewById(R.id.layout) as android.support.constraint.ConstraintLayout
        val background = Canvass(this)
        layout1.addView(background)

        mAddress = intent.getStringExtra(DeviceListActivity.EXTRA_ADDRESS)
        ConnectToDevice(this).execute()
        control_forward.setOnClickListener { sendCommand("f") }
        control_left.setOnClickListener { sendCommand("l") }
        control_right.setOnClickListener { sendCommand("r") }
        control_back.setOnClickListener { sendCommand("b") }
        control_led_disconnect.setOnClickListener { disconnect() }
    }

    class Canvass (context: Context): View(context) {

        override fun onDraw (canvas: Canvas) {
            canvas.drawRGB (255, 255, 255)
            val width = getWidth ()
            val hieght = getHeight ()
            val brush1 = Paint ()
            val brush2 = Paint()
            brush1.setARGB (255, 0, 0, 0)
            brush2.setARGB(255, 255, 0, 0)
            brush1.setStyle (Paint.Style.STROKE)

            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(),5.toFloat(), brush2)
            canvas.drawCircle((width/2).toFloat(), (height - (height/3)).toFloat(), 340.toFloat(), brush1)

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