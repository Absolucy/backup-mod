package moe.absolucy.backup.common.utils

import com.appmattus.crypto.Algorithm
import java.io.OutputStream

/**
 * An [OutputStream] that calculates a digest of the data written to it.
 * @param algorithm The digest algorithm to use.
 * @param passthroughOutputStream The output stream to pass data through into.
 * @see [com.appmattus.crypto.Digest]
 */
internal class DigestOutputStream(algorithm: Algorithm, private val passthroughOutputStream: OutputStream? = null) : OutputStream() {
	private val digest = algorithm.createDigest()

	fun digest(): ByteArray = digest.digest()

	override fun write(byte: Int) {
		digest.update(byte.toByte())
		passthroughOutputStream?.write(byte)
	}

	override fun write(bytes: ByteArray) {
		digest.update(bytes)
		passthroughOutputStream?.write(bytes)
	}

	override fun write(bytes: ByteArray, offset: Int, length: Int) {
		digest.update(bytes, offset, length)
		passthroughOutputStream?.write(bytes, offset, length)
	}

	override fun flush() {
		passthroughOutputStream?.flush()
	}

	override fun close() {
		passthroughOutputStream?.close()
	}
}