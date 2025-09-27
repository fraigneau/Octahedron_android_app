package com.octahedron.service

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import com.octahedron.data.ble.BluetoothDeviceManager
import com.octahedron.data.ble.Packet
import com.octahedron.data.bus.NowPlayingBus
import com.octahedron.data.image.ImageTools
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.CRC32
import kotlin.coroutines.resume

class BlePacketManager : Service() {

    companion object {
        private const val TAG = "BlePacketManager"
        // TODO: recup de la config DataStore
        private const val MAC = "A0:85:E3:EA:14:09" // MAC stable de ton ESP32
    }

    private lateinit var manager: BluetoothDeviceManager
    private val binder = LocalBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + serviceJob)

    @Volatile private var bleReady = false
    @Volatile private var wantStayConnected = true
    @Volatile private var lastDisplayedCrc: Long? = null

    private var flowJob: Job? = null
    private var keepAliveJob: Job? = null
    private var reconnectJob: Job? = null

    private val LINE_WIDTH_PX = 240
    private val BYTES_PER_PIXEL = 2
    private val LINE_SIZE = LINE_WIDTH_PX * BYTES_PER_PIXEL

    inner class LocalBinder : Binder()

    override fun onCreate() {
        super.onCreate()
        manager = BluetoothDeviceManager(applicationContext)
        hookConnectionObserver()
        startCollectingNowPlaying()

        ensureConnected()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        wantStayConnected = false
        flowJob?.cancel()
        stopKeepAlive()
        reconnectJob?.cancel()
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun hookConnectionObserver() {
        manager.setConnectionObserver(object : ConnectionObserver {
            override fun onDeviceConnecting(device: BluetoothDevice) {
                Log.i(TAG, "Connecting to ${device.address}")
                bleReady = false
            }
            override fun onDeviceConnected(device: BluetoothDevice) {
                Log.i(TAG, "Connected ${device.address} (GATT opened)")
            }
            override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
                Log.w(TAG, "Failed to connect ${device.address}: ${reason.toConnReason()}")
                bleReady = false
                scheduleReconnect(device)
            }
            override fun onDeviceReady(device: BluetoothDevice) {
                Log.i(TAG, "Device ready ${device.address} (services ok, MTU/notifs set)")
                bleReady = true
                startKeepAlive()
            }
            override fun onDeviceDisconnecting(device: BluetoothDevice) {
                Log.i(TAG, "Disconnecting ${device.address}")
                bleReady = false
                stopKeepAlive()
            }
            override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
                Log.w(TAG, "Disconnected ${device.address}: ${reason.toConnReason()}")
                bleReady = false
                stopKeepAlive()
                scheduleReconnect(device)
            }
        })
    }

    private fun startKeepAlive() {
        keepAliveJob?.cancel()
        keepAliveJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive && wantStayConnected) {
                if (manager.isConnected && bleReady) {
                    manager.sendAndWaitForResponse(
                        payload = Packet.Build.HealthAsk(),
                        onResponse = { _, data ->
                            val bytes = data.value ?: run {
                                Log.w(TAG, "HealthCheck: réponse invalide (null)")
                                return@sendAndWaitForResponse
                            }
                            val status = Packet.parse(bytes)
                            if (status == Packet.Parsed.Health(Packet.HealthStatus.OK)) {
                                return@sendAndWaitForResponse
                            }
                            Log.w(TAG, "HealthCheck: réponse invalide (status=$status)")
                        },
                        onFail = { _, status ->
                            Log.w(TAG, "HealthCheck failed: $status")
                        },
                        timeoutMs = 1_000
                    )
                }
                delay(30_000)
            }
        }
    }

    private fun stopKeepAlive() {
        keepAliveJob?.cancel()
        keepAliveJob = null
    }

    private fun scheduleReconnect(device: BluetoothDevice) {
        if (!wantStayConnected) return
        if (reconnectJob?.isActive == true) return

        reconnectJob = serviceScope.launch {
            var delayMs = 1_000L
            val maxDelay = 60_000L
            while (isActive && wantStayConnected && !manager.isConnected) {
                if (manager.isConnected) { delay(1_000); continue }

                val attached = CompletableDeferred<Boolean>()
                manager.connect(device)
                    .useAutoConnect(true)
                    .retry(0, 0)
                    .done {
                        bleReady = true
                        startKeepAlive()
                        attached.complete(true)
                    }
                    .fail { _, _ -> bleReady = false; attached.complete(false) }
                    .enqueue()

                if (attached.await()) break

                val found = try {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                        scanOnce(serviceUuid = BluetoothDeviceManager.SERVICE_UUID, mac = MAC, timeoutMs = 8_000) != null
                    } else {
                        Log.w(TAG, "Permission BLUETOOTH_SCAN not granted")
                        false
                    }
                } catch (_: Throwable) { false }

                if (found && !manager.isConnected) {
                    val ok = CompletableDeferred<Boolean>()
                    manager.connect(device)
                        .useAutoConnect(false)
                        .retry(2, 200)
                        .done { bleReady = true; startKeepAlive(); ok.complete(true) }
                        .fail { _, _ -> bleReady = false; ok.complete(false) }
                        .enqueue()
                    if (ok.await()) break
                }

                delay(delayMs)
                delayMs = (delayMs * 2).coerceAtMost(maxDelay)
            }
        }
    }

    private fun ensureConnected() {
        if (!wantStayConnected) return
        val dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MAC)
        manager.connect(dev)
            .useAutoConnect(false)
            .retry(2, 200)
            .done { bleReady = true; startKeepAlive() }
            .fail { _, _ -> Log.w(TAG, "ensureConnected échec"); bleReady = false; scheduleReconnect(dev) }
            .enqueue()
    }

    private fun startCollectingNowPlaying() {
        flowJob?.cancel()
        flowJob = serviceScope.launch {
            NowPlayingBus.flow.collectLatest { np ->
                Log.i(TAG, "Now playing: $np, bleReady=$bleReady, bitmap=${np.bitmap}")
                ensureConnectedAndSend(np.bitmap)
            }
        }
    }

    private fun ensureConnectedAndSend(bmp: Bitmap) {
        if (manager.isConnected && bleReady) {
            startSendCover(bmp)
            return
        }
        val dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MAC)
        scheduleReconnect(dev)
        serviceScope.launch {
            repeat(30) { // ~15 s max
                if (manager.isConnected && bleReady) {
                    startSendCover(bmp); return@launch
                }
                delay(500)
            }
            Log.w(TAG, "Impossible d’envoyer: pas reconnecté à temps")
        }
    }

    private fun startSendCover(img: Bitmap) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val imgBytes = ImageTools.toRgb565BytesAuto(img)

                val crc32 = CRC32().apply { update(imgBytes) }
                if (lastDisplayedCrc == crc32.value) {
                    Log.i(TAG, "Même cover (CRC identique), envoi ignoré")
                    return@launch
                }

                val isOnSd = sendFileExists(crc32)
                if (isOnSd) {
                    Log.i(TAG, "Le fichier est déjà présent, on skip l'envoi")
                } else {
                    sendChunkImg(crc32, imgBytes)
                }

                val displayOk = sendFileDisplay(crc32)
                if (!displayOk) Log.w(TAG, "Avertissement: FileDisplay non confirmé")
                else lastDisplayedCrc = crc32.value

            } catch (t: Throwable) {
                Log.e(TAG, "startSendCover error", t)
            }
        }
    }

    private suspend fun sendFileExists(crc32: CRC32): Boolean {
        val existsDeferred = CompletableDeferred<Boolean>()

        manager.sendAndWaitForResponse(
            payload = Packet.Build.FileExists(crc32),
            onResponse = { _, data ->
                val bytes = data.value
                if (bytes == null) {
                    existsDeferred.complete(false)
                    return@sendAndWaitForResponse
                }
                val parsed = Packet.parse(bytes)
                val exists = (parsed is Packet.Parsed.FileExists) && parsed.exists
                existsDeferred.complete(exists)
            },
            onFail = { _, status -> Log.w(TAG, "FileExists failed: $status"); existsDeferred.complete(false) },
            timeoutMs = 1_000
        )

        return existsDeferred.await()
    }

    private suspend fun sendChunkImg(crc32: CRC32, imgBytes: ByteArray) {
        val buf = ByteArray(LINE_SIZE)
        var offset = 0

        while (offset < imgBytes.size && manager.isConnected) {
            val end = minOf(offset + LINE_SIZE, imgBytes.size)
            val len = end - offset
            System.arraycopy(imgBytes, offset, buf, 0, len)
            val chunk = if (len == LINE_SIZE) buf else buf.copyOf(len)

            val done = CompletableDeferred<Boolean>()
            manager.send(
                payload = Packet.Build.FileWrite(crc32, chunk),
                onSent = { _, _ -> done.complete(true) },
                onFail = { _, status -> Log.e(TAG, "Failed chunk: $status"); done.complete(false) }
            )
            if (!done.await()) break
            offset = end
        }
    }

    private suspend fun sendFileDisplay(crc32: CRC32): Boolean {
        val ack = CompletableDeferred<Boolean>()
        manager.send(
            payload = Packet.Build.FileDisplay(crc32),
            onSent = { _, _ -> ack.complete(true) },
            onFail = { _, status ->
                Log.e(TAG, "Failed FileDisplay: $status")
                ack.complete(false)
            }
        )
        return ack.await()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private suspend fun scanOnce(
        serviceUuid: UUID? = null,
        mac: String? = null,
        timeoutMs: Long = 10_000L
    ): BluetoothDevice? = suspendCancellableCoroutine { cont ->
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val scanner = adapter?.bluetoothLeScanner
        if (adapter == null || !adapter.isEnabled || scanner == null) {
            cont.resume(null); return@suspendCancellableCoroutine
        }

        val filters = when {
            serviceUuid != null && mac != null ->
                listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUuid)).setDeviceAddress(mac).build())
            serviceUuid != null ->
                listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUuid)).build())
            mac != null ->
                listOf(ScanFilter.Builder().setDeviceAddress(mac).build())
            else -> emptyList()
        }
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        val resumed = AtomicBoolean(false)
        lateinit var cb: ScanCallback
        fun stop() = runCatching { scanner.stopScan(cb) }

        cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (resumed.compareAndSet(false, true)) { stop(); cont.resume(result.device) }
            }
            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                val first = results.firstOrNull()
                if (first != null && resumed.compareAndSet(false, true)) { stop(); cont.resume(first.device) }
            }
            override fun onScanFailed(errorCode: Int) {
                if (resumed.compareAndSet(false, true)) { stop(); cont.resume(null) }
            }
        }

        try { scanner.startScan(filters, settings, cb) }
        catch (_: Throwable) { if (resumed.compareAndSet(false, true)) cont.resume(null); return@suspendCancellableCoroutine }

        val handler = Handler(Looper.getMainLooper())
        val timeout = Runnable {
            if (resumed.compareAndSet(false, true)) { stop(); cont.resume(null) }
        }
        handler.postDelayed(timeout, timeoutMs)
        cont.invokeOnCancellation { handler.removeCallbacks(timeout); stop() }
    }

    private fun Int.toConnReason(): String = when (this) {
        ConnectionObserver.REASON_UNKNOWN            -> "UNKNOWN"
        ConnectionObserver.REASON_SUCCESS            -> "SUCCESS"
        ConnectionObserver.REASON_TIMEOUT            -> "TIMEOUT"
        ConnectionObserver.REASON_CANCELLED          -> "CANCELLED"
        ConnectionObserver.REASON_LINK_LOSS          -> "LINK_LOSS"
        ConnectionObserver.REASON_NOT_SUPPORTED      -> "NOT_SUPPORTED"
        else                                         -> "CODE_$this"
    }
}