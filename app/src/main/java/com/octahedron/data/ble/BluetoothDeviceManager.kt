package com.octahedron.data.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.callback.DataSentCallback
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.data.Data
import java.util.UUID

class BluetoothDeviceManager(context: Context) : BleManager(context) {

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AB")
        private val CHAR_TX_NOTIFY: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AC")
        private val CHAR_RX_WRITE: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AC")
        const val TAG = "BluetoothDeviceManager"
    }

    private var txNotify: BluetoothGattCharacteristic? = null
    private var rxWrite: BluetoothGattCharacteristic? = null
    private var externalNotifyCb: DataReceivedCallback? = null

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(SERVICE_UUID)
        if (service == null) return false

        txNotify = service.getCharacteristic(CHAR_TX_NOTIFY)
        rxWrite = service.getCharacteristic(CHAR_RX_WRITE)

        val notifyOk = txNotify != null
                && (txNotify!!.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0

        val writeOk = rxWrite != null
                && ((rxWrite!!.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
                || (rxWrite!!.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0)

        return notifyOk && writeOk
    }

    override fun initialize() {
        requestMtu(517)
            .with { _, mtu -> Log.i(TAG, "MTU négocié=$mtu") }
            .fail { _, st -> Log.w(TAG, "MTU fail=$st, on continue en défaut") }
            .enqueue()

        requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH).enqueue()

        setNotificationCallback(txNotify).with { device, data ->
            externalNotifyCb?.onDataReceived(device, data)
        }
        enableNotifications(txNotify)
            .done { Log.i(TAG, "Notifications activées") }
            .fail { _, status -> Log.w(TAG, "Enable notifications échec: $status") }
            .enqueue()
    }

    fun send(payload: ByteArray, onSent: DataSentCallback?, onFail: FailCallback?) {
        val c = rxWrite ?: return onFail?.onRequestFailed(bluetoothDevice!!, FailCallback.REASON_NULL_ATTRIBUTE) ?: Unit
        writeCharacteristic(c, payload, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
            //.split()
            .done { d -> onSent?.onDataSent(d, Data(payload)) }
            .fail { d, s -> onFail?.onRequestFailed(d, s) }
            .enqueue()
    }

    fun sendAndWaitForResponse(
        payload: ByteArray,
        onResponse: DataReceivedCallback,
        onFail: FailCallback?,
        timeoutMs: Long
    ) {
        val cW = rxWrite ?: return onFail?.onRequestFailed(bluetoothDevice!!, FailCallback.REASON_NULL_ATTRIBUTE) ?: Unit
        val cN = txNotify ?: return onFail?.onRequestFailed(bluetoothDevice!!, FailCallback.REASON_NULL_ATTRIBUTE) ?: Unit

        beginAtomicRequestQueue()
            .add(writeCharacteristic(cW, payload, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT).split())
            .add(waitForNotification(cN).with(onResponse).timeout(timeoutMs))
            .fail { d, s -> onFail?.onRequestFailed(d, s) }
            .enqueue()
    }
}
