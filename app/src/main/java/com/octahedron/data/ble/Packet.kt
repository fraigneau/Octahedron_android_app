package com.octahedron.data.ble

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

object Packet {

    const val TAG = "Packet"

    const val MAGIC = 0x2052464C // "LFR "
    const val HEADER_SIZE = 6
    const val HASH_SIZE = 4
    const val PIXEL_PER_LINE = 240
    const val BYTES_PER_PIXEL = 2
    const val LINE_SIZE = PIXEL_PER_LINE * BYTES_PER_PIXEL
    const val HEALTH_STATUS_SIZE = 1

    object HealthStatus {
        const val OK: Byte = 0
        const val DISPLAY_ERROR: Byte = 1
        const val SD_ERROR: Byte = 2
        const val UNK_ERROR: Byte = 3
        const val ASKED: Byte = 4
    }

    object Direction {
        const val DIR_ESP_TO_DEVICE: Byte = 'E'.code.toByte()
        const val DIR_DEVICE_TO_ESP: Byte = 'D'.code.toByte()
    }

    object Type {
        const val HEALTH: Byte = 0
        const val CURRENTLY_PLAYING: Byte = 1 // idk if needed
        const val FILE_EXISTS: Byte = 2
        const val FILE_WRITE: Byte = 3
        const val FILE_WRITE_RESPONSE: Byte = 4
        const val FILE_DISPLAY: Byte = 5 // idk if needed
        const val FILE_DELETE: Byte = 6
    }

    object Response {
        const val OK: Byte = 0
        const val ERROR: Byte = 1
    }

    data class Base(
        val magic: Int,
        val direction: Byte,
        val type: Byte,
    ){
        companion object         {
            fun make(magic: Int = MAGIC, direction: Byte = Direction.DIR_DEVICE_TO_ESP, type: Byte): Base {
                return Base(magic, direction, type)
            }

            fun parse(bytes: ByteArray, offset: Int = 0): Base? {
                require(bytes.size - offset >= HEADER_SIZE) { "Packet too small" }
                val header = ByteBuffer.wrap(bytes, offset, 6)
                val magic = header.int
                val direction = header.get()
                val type = header.get()
                if (magic != MAGIC) return null
                return Base(magic, direction, type)
            }
        }

        fun toBytes(): ByteArray {
            val buffer = ByteBuffer.allocate(HEADER_SIZE)
            buffer.putInt(magic)
            buffer.put(direction)
            buffer.put(type)
            return buffer.array()
        }
    }

    object Build {
        fun HealthAsk(): ByteArray {
            val base = Base.make(type = Type.HEALTH).toBytes()

            Log.i("BluetoothGatt", "HealthAsk base: ${base.size} bytes")

            val buffer = ByteBuffer.allocate(HEADER_SIZE + HEALTH_STATUS_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            buffer.put(base)
            buffer.put(HealthStatus.ASKED)

            return buffer.array()
        }
        fun FileWrite(crc32: CRC32, line: ByteArray): ByteArray {
            val base = Base.make(type = Type.FILE_WRITE).toBytes()
            val hashByte = ByteBuffer.allocate(HASH_SIZE).putInt(crc32.value.toInt()).array()

            val buffer = ByteBuffer.allocate(HEADER_SIZE + HASH_SIZE + LINE_SIZE)
            buffer.put(base)
            buffer.put(hashByte)
            buffer.put(line)

            return buffer.array()
        }
        fun FileExists(crc32: CRC32): ByteArray {
            val base = Base.make(type = Type.FILE_EXISTS).toBytes()
            val hashByte = ByteBuffer.allocate(HASH_SIZE).putInt(crc32.value.toInt()).array()
            val ok = 0.toByte()


            val buffer = ByteBuffer.allocate(HEADER_SIZE + HASH_SIZE + 1)
            buffer.put(base)
            buffer.put(hashByte)
            buffer.put(ok)

            return buffer.array()
        }
        fun FileDelete(crc32: CRC32): ByteArray {
            val base = Base.make(type = Type.FILE_DELETE).toBytes()
            val hashByte = ByteBuffer.allocate(HASH_SIZE).putInt(crc32.value.toInt()).array()

            val buffer = ByteBuffer.allocate(HEADER_SIZE + HASH_SIZE)
            buffer.put(base)
            buffer.put(hashByte)

            return buffer.array()
        }
        fun FileDisplay(crc32: CRC32): ByteArray {
            val base = Base.make(type = Type.FILE_DISPLAY).toBytes()
            val hashByte = ByteBuffer.allocate(HASH_SIZE).putInt(crc32.value.toInt()).array()

            val buffer = ByteBuffer.allocate(HEADER_SIZE + HASH_SIZE + 1)
            buffer.put(base)
            buffer.put(hashByte)
            buffer.put(0)

            return buffer.array()
        }
    }

    sealed interface Parsed {
        data class Health(val status: Byte) : Parsed
        data class FileExists(val exists: Boolean) : Parsed
        data class FileWriteResponse(val ok: Boolean) : Parsed
        data class Error(val reason: String) : Parsed
    }

    fun parse(packet: ByteArray?): Parsed {
        if (packet == null) return Parsed.Error("null packet")

        val bb = ByteBuffer.wrap(packet)

        if (bb.limit() < HEADER_SIZE + 1) return Parsed.Error("too short")

        val baseHeader = ByteArray(HEADER_SIZE).also {
            val dup = bb.duplicate()
            dup.position(0); dup.limit(HEADER_SIZE)
            dup.get(it)
        }

        val base = Base.parse(baseHeader) ?: return Parsed.Error("base parse failed")
        if (base.magic != MAGIC) return Parsed.Error("bad magic")
        if (base.direction != Direction.DIR_ESP_TO_DEVICE) return Parsed.Error("bad direction")

        // -- payload view
        val payload = bb.duplicate().apply {
            position(HEADER_SIZE)
            limit(bb.limit())
        }

        return when (base.type) {
            Type.HEALTH -> {
                if (payload.remaining() < 1) return Parsed.Error("health payload too short")
                val s = payload.get()
                Parsed.Health(status = s)
            }
            Type.FILE_EXISTS -> {
                if (payload.remaining() < 1) return Parsed.Error("exists payload too short")
                val last = run {
                    val d = payload.duplicate()
                    d.position(d.limit() - 1)
                    d.get()
                }
                Parsed.FileExists(exists = (last.toInt() != 0))
            }
            Type.FILE_WRITE_RESPONSE -> {
                val ok = true
                Parsed.FileWriteResponse(ok)
            }
            else -> Parsed.Error("unknown type ${base.type}")
        }
    }
}