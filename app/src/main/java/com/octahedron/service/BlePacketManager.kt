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
import com.octahedron.data.bus.NowPlayingBus
import com.octahedron.data.image.ImageTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.zip.CRC32


class BlePacketManager: Service() {

    companion object {
        const val TAG = "BlePacketManager"
    }
    lateinit var manager: MyEsp32Manager
    private val binder = LocalBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + serviceJob)

    @Volatile private var bleReady = false
    private var flowJob: Job? = null
    inner class LocalBinder : Binder() { }

    private fun startCollectingNowPlaying() {
        flowJob?.cancel()
        flowJob = serviceScope.launch {
            NowPlayingBus.flow.collectLatest { np ->
                Log.i(TAG, "Now playing: $np, bleready=$bleReady and bitmap=${np.bitmap}")
                if (bleReady && np.bitmap != null) {
                    startSendCover(np.bitmap)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        manager = MyEsp32Manager(applicationContext)
        Log.i( TAG, "Service created" )

        // TODO : get the device address from DataStore settings
        val device: BluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("A0:85:E3:EA:14:09")

        startCollectingNowPlaying()

        connect(device)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun connect(device: BluetoothDevice) {
        manager.connect(device)
            .useAutoConnect(false)
            .retry(3, 100)
            .done {
                bleReady = true
                Log.i(TAG, "Connexion réussie")
                NowPlayingBus.lastOrNull()?.bitmap?.let { bmp ->
                    startSendCover(bmp)
                }
            }
            .fail { d, status ->
                Log.e(TAG, "Connexion échouée: $status")
            }
            .enqueue()
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
            onSent = { _, _ -> Log.i(TAG, "FileDisplay sent") },
            onFail = { _, status -> Log.e(TAG, "Failed to send cover display command : $status") }
        )
    }

    fun disconnect() {
        manager.disconnect().enqueue()
        bleReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        flowJob?.cancel()
        serviceJob.cancel()
    }
}