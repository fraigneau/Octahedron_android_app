package com.octahedron.data.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.callback.DataSentCallback
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.callback.MtuCallback
import no.nordicsemi.android.ble.callback.SuccessCallback
import no.nordicsemi.android.ble.data.Data
import java.util.UUID


class MyEsp32Manager(context: Context) : BleManager(context) {
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
        requestMtu(200)
            .with(MtuCallback { device: BluetoothDevice?, mtu: Int ->
                log(
                    Log.INFO,
                    "MTU négocié = $mtu"
                )
            })
            .enqueue()

        requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH).enqueue()

        setNotificationCallback(txNotify).with(DataReceivedCallback { device: BluetoothDevice?, data: Data? ->
            if (externalNotifyCb != null) externalNotifyCb!!.onDataReceived(
                device!!, data!!
            )
        })
        enableNotifications(txNotify)
            .done(SuccessCallback { d: BluetoothDevice? ->
                log(
                    Log.INFO,
                    "Notifications activées"
                )
            })
            .fail(FailCallback { d: BluetoothDevice?, status: Int ->
                log(
                    Log.WARN,
                    "Enable notifications échec: $status"
                )
            })
            .enqueue()
    }

    override fun onServicesInvalidated() {
        txNotify = null
        rxWrite = null
    }

    fun setOnNotificationReceived(cb: DataReceivedCallback?) {
        this.externalNotifyCb = cb
        if (txNotify != null && cb != null) {
            setNotificationCallback(txNotify).with(cb)
        }
    }

    fun send(payload: ByteArray?, onSent: DataSentCallback?, onFail: FailCallback?) {
        if (rxWrite == null) {
            onFail?.onRequestFailed(
                bluetoothDevice!!,
                FailCallback.REASON_NULL_ATTRIBUTE
            )
            return
        }
        val writeType =
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

        writeCharacteristic(rxWrite, payload!!, writeType)
            //.split()
            .done(SuccessCallback { d: BluetoothDevice? ->
                onSent?.onDataSent(
                    d!!,
                    Data.from(payload.toString())
                )
            })
            .fail(FailCallback { d: BluetoothDevice?, status: Int ->
                onFail?.onRequestFailed(
                    d!!, status
                )
            })
            .enqueue()
    }

    fun send(data: Data, onSent: DataSentCallback?, onFail: FailCallback?) {
        send(data.value, onSent, onFail)
    }

    fun sendAndWaitForResponse(
        cmd: ByteArray?,
        onResponse: DataReceivedCallback,
        onFail: FailCallback?,
        timeoutMs: Long
    ) {
        if (rxWrite == null || txNotify == null) {
            onFail?.onRequestFailed(
                bluetoothDevice!!,
                FailCallback.REASON_NULL_ATTRIBUTE
            )
            return
        }

        val writeType =
            if ((rxWrite!!.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0)
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        beginAtomicRequestQueue()
            .add(
                writeCharacteristic(rxWrite, cmd, writeType)
            )
            .add(
                waitForNotification(txNotify)
                    .with(onResponse)
                    .fail { d: BluetoothDevice?, status: Int ->
                        onFail?.onRequestFailed(
                            d!!, status
                        )
                    }
                    .timeout(timeoutMs)
            )
            .enqueue()
    }

    companion object {
        private val SERVICE_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AB")
        private val CHAR_TX_NOTIFY: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AC")
        private val CHAR_RX_WRITE: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AC")
    }
}
