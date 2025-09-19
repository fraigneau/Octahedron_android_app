package com.octahedron.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.octahedron.data.ble.MyEsp32Manager
import com.octahedron.data.ble.packet.Packet
import com.octahedron.data.image.ImageTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.zip.CRC32


class BlePacketManager: Service() {

    val TAG = "BlePacketManager"
    lateinit var manager: MyEsp32Manager
    private var periodicSender: Job? = null
    private var coverCollector: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val coverFlow = MutableSharedFlow<Bitmap>(
        replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): BlePacketManager = this@BlePacketManager
    }

    override fun onCreate() {
        super.onCreate()
        manager = MyEsp32Manager(applicationContext)
        Log.i( TAG, "Service created" )

        // TODO : get the device address from DataStore settings
        val device: BluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("A0:85:E3:EA:14:09")

        connect(device)

        coverCollector = serviceScope.launch {
            coverFlow.collectLatest { bmp ->
                startSendCover(bmp)
                Log.i(TAG, "Cover sent")
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder = binder

    fun connect(device: BluetoothDevice) {
        manager.connect(device)
            .useAutoConnect(false)
            .retry(3, 100)
            .done {
                Log.i(TAG, "Connexion réussie")
                //startPeriodicHealthAsk()
            }
            .fail { d, status ->
                Log.e(TAG, "Connexion échouée: $status")
            }
            .enqueue()
    }

    private fun startPeriodicHealthAsk() {
        periodicSender?.cancel()

        periodicSender = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && manager.isConnected) {
                manager.send(
                    payload = Packet.Build.HealthAsk(),
                    onSent = { _, data -> Log.i(TAG, "Data sent: ${data.value}") },
                    onFail = { _, status -> Log.e(TAG, "Failed to send data: $status") }
                )
                delay(30_000)
            }
            Log.d(TAG, "Periodic sender stopped (disconnected or cancelled)")
        }
    }

    fun sendCover(bitmap: Bitmap) {
        val safeBmp = bitmap.copy(Bitmap.Config.RGB_565, false)
        coverFlow.tryEmit(safeBmp)
    }

    private fun startSendCover(img: Bitmap) {
        Log.i(TAG, "Cover sending started")
        val imgBytes = ImageTools.toRgb565BytesAuto(img)
        val crc32 = CRC32().apply { update(imgBytes) }

        val lineSize = 240 * 2
        val chunks = mutableListOf<ByteArray>()

        for (i in imgBytes.indices step lineSize) {
            val end = minOf(i + lineSize, imgBytes.size)
            val chunk = imgBytes.copyOfRange(i, end)
            chunks.add(chunk)
        }

        for (chunk in chunks) {
            manager.send(
                payload = Packet.Build.FileWrite(crc32, chunk),
                onSent = { _, _ -> },
                onFail = { _, status -> Log.e(TAG, "Failed to send cover chunk: $status") }
            )
        }

        manager.send(
            payload = Packet.Build.FileDisplay(crc32),
            onSent = { _, _ -> },
            onFail = { _, status -> Log.e(TAG, "Failed to send cover display command : $status") }
        )

        Log.d(TAG, "Cover sending completed or stopped (disconnected)")

    }

    fun disconnect() {
        manager.disconnect().enqueue()
    }
}