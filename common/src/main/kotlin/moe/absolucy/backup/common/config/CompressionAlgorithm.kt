package moe.absolucy.backup.common.config

import com.github.luben.zstd.Zstd
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.absolucy.backup.common.LBMMod
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.compressors.gzip.GzipParameters
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.LZMAOutputStream
import org.tukaani.xz.XZOutputStream
import java.io.OutputStream
import java.util.zip.Deflater

/**
 * The result of a compression level query.
 */
sealed class CompressionLevelResult {
	/**
	 * The compression level is unused by this algorithm.
	 */
	object Unused : CompressionLevelResult()

	/**
	 * The compression level is valid for this algorithm.
	 */
	object Valid : CompressionLevelResult()

	/**
	 * The compression level is invalid for this algorithm.
	 * @param min The minimum valid compression level.
	 * @param max The maximum valid compression level.
	 * @param default The default compression level.
	 */
	data class Invalid(val min: Int, val max: Int, val default: Int) : CompressionLevelResult()
}

/**
 * The min/max/default compression levels for Zstandard,
 * so we don't do a JNI call every time we need to check.
 */
private object ZstdCompressionLevels {
	/**
	 * The minimum compression level.
	 */
	val MIN = Zstd.minCompressionLevel()

	/**
	 * The maximum compression level.
	 */
	val MAX = Zstd.maxCompressionLevel()

	/**
	 * The range of valid compression levels.
	 */
	val RANGE = MIN..MAX

	/**
	 * The default compression level.
	 */
	val DEFAULT = Zstd.defaultCompressionLevel()
}

/**
 * Compression algorithms.
 */
@Serializable
enum class CompressionAlgorithm {
	/**
	 * No compression.
	 * Uses the `.tar` extension.
	 */
	@SerialName("none")
	NONE {
		override fun getExtension(): String = "tar"
		override fun getOutputStream(input: OutputStream): OutputStream = input
		override fun defaultCompressionLevel(): Int = 0
		override fun inspectCompressionLevel(level: Int): CompressionLevelResult = CompressionLevelResult.Unused
	},

	/**
	 * BZIP2 compression.
	 * Uses the `.tar.bz2` extension.
	 * @see <a href="https://sourceware.org/bzip2">libbzip2 home page</a>
	 * @see <a href="https://commons.apache.org/proper/commons-compress/apidocs/org/apache/commons/compress/compressors/bzip2/BZip2CompressorOutputStream.html">Apache Commons Compress: BZip2CompressorOutputStream</a>
	 */
	@SerialName("bz2")
	BZIP2 {
		override fun getExtension(): String = "tar.bz2"
		override fun getOutputStream(input: OutputStream): OutputStream = BZip2CompressorOutputStream(input)
		override fun defaultCompressionLevel(): Int = -1
		override fun inspectCompressionLevel(level: Int): CompressionLevelResult = CompressionLevelResult.Unused
	},

	/**
	 * GZIP compression.
	 * Uses the `.tar.gz` extension.
	 * @see <a href="https://www.gzip.org">gzip home page</a>
	 * @see <a href="https://commons.apache.org/proper/commons-compress/apidocs/org/apache/commons/compress/compressors/gzip/GzipCompressorOutputStream.html">Apache Commons Compress: GzipCompressorOutputStream</a>
	 */
	@SerialName("gz")
	GZIP {
		override fun getExtension(): String = "tar.gz"
		override fun getOutputStream(input: OutputStream): OutputStream = GzipCompressorOutputStream(input, GzipParameters().apply {
			this.compressionLevel = LBMMod.CONFIG.compressionLevel
		})

		override fun defaultCompressionLevel(): Int = Deflater.DEFAULT_COMPRESSION

		override fun inspectCompressionLevel(level: Int): CompressionLevelResult {
			return if (level in Deflater.DEFAULT_COMPRESSION..Deflater.BEST_COMPRESSION) {
				CompressionLevelResult.Valid
			} else {
				CompressionLevelResult.Invalid(Deflater.DEFAULT_COMPRESSION, Deflater.BEST_COMPRESSION, Deflater.DEFAULT_COMPRESSION)
			}
		}
	},

	/**
	 * Framed LZ4 compression.
	 * Uses the `.tar.lz4` extension.
	 * @see <a href="https://lz4.github.io/lz4/">lz4 home page</a>
	 * @see <a href="https://commons.apache.org/proper/commons-compress/apidocs/org/apache/commons/compress/compressors/lz4/FramedLZ4CompressorOutputStream.html">Apache Commons Compress: FramedLZ4CompressorOutputStream</a>
	 */
	@SerialName("lz4")
	LZ4 {
		override fun getExtension(): String = "tar.lz4"
		override fun getOutputStream(input: OutputStream): OutputStream = FramedLZ4CompressorOutputStream(input)
		override fun defaultCompressionLevel(): Int = -1
		override fun inspectCompressionLevel(level: Int): CompressionLevelResult = CompressionLevelResult.Unused
	},

	/**
	 * LZMA compression.
	 * Uses the `.tar.lzma` extension.
	 * @see <a href="https://tukaani.org/xz/">xz home page</a>
	 * @see <a href="https://commons.apache.org/proper/commons-compress/apidocs/org/apache/commons/compress/compressors/lzma/LZMACompressorOutputStream.html">Apache Commons Compress: LZMACompressorOutputStream</a>
	 */
	@SerialName("lzma")
	LZMA {
		override fun getExtension(): String = "tar.lzma"
		override fun getOutputStream(input: OutputStream): OutputStream = LZMAOutputStream(input, LZMA2Options(LBMMod.CONFIG.compressionLevel), -1)
		override fun defaultCompressionLevel(): Int = LZMA2Options.PRESET_DEFAULT

		override fun inspectCompressionLevel(level: Int): CompressionLevelResult {
			return if (level in LZMA2Options.PRESET_MIN..LZMA2Options.PRESET_MAX) {
				CompressionLevelResult.Valid
			} else {
				CompressionLevelResult.Invalid(LZMA2Options.PRESET_MIN, LZMA2Options.PRESET_MAX, LZMA2Options.PRESET_DEFAULT)
			}
		}
	},

	/**
	 * Compress as a 7Z archive, using LZMA2, rather than a .tar.ext archive.
	 * Uses the `.7z` extension.
	 */
	@SerialName("7z")
	SEVENZ {
		override fun getExtension(): String = "7z"
		override fun getOutputStream(input: OutputStream): OutputStream = throw NotImplementedError("This should NOT happen!")
		override fun defaultCompressionLevel(): Int = LZMA2Options.PRESET_DEFAULT

		override fun inspectCompressionLevel(level: Int): CompressionLevelResult {
			return if (level in LZMA2Options.PRESET_MIN..LZMA2Options.PRESET_MAX) {
				CompressionLevelResult.Valid
			} else {
				CompressionLevelResult.Invalid(LZMA2Options.PRESET_MIN, LZMA2Options.PRESET_MAX, LZMA2Options.PRESET_DEFAULT)
			}
		}
	},

	/**
	 * XZ compression.
	 * Uses the `.tar.xz` extension.
	 * @see <a href="https://tukaani.org/xz/">xz home page</a>
	 * @see <a href="https://commons.apache.org/proper/commons-compress/apidocs/org/apache/commons/compress/compressors/xz/XZCompressorOutputStream.html">Apache Commons Compress: XZCompressorOutputStream</a>
	 */
	@SerialName("xz")
	XZ {
		override fun getExtension(): String = "tar.xz"
		override fun getOutputStream(input: OutputStream): OutputStream = XZOutputStream(input, LZMA2Options(LBMMod.CONFIG.compressionLevel), -1)
		override fun defaultCompressionLevel(): Int = LZMA2Options.PRESET_DEFAULT

		override fun inspectCompressionLevel(level: Int): CompressionLevelResult {
			return if (level in LZMA2Options.PRESET_MIN..LZMA2Options.PRESET_MAX) {
				CompressionLevelResult.Valid
			} else {
				CompressionLevelResult.Invalid(LZMA2Options.PRESET_MIN, LZMA2Options.PRESET_MAX, LZMA2Options.PRESET_DEFAULT)
			}
		}
	},

	/**
	 * Zstandard compression.
	 * Uses the `.tar.zst` extension.
	 * @see <a href="https://facebook.github.io/zstd/">zstd home page</a>
	 * @see <a href="https://commons.apache.org/proper/commons-compress/apidocs/org/apache/commons/compress/compressors/zstandard/ZstdCompressorOutputStream.html">Apache Commons Compress: ZstdCompressorOutputStream</a>
	 */
	@SerialName("zstd")
	ZSTANDARD {
		override fun getExtension(): String = "tar.zst"
		override fun getOutputStream(input: OutputStream): OutputStream = ZstdCompressorOutputStream(input, LBMMod.CONFIG.compressionLevel)
		override fun defaultCompressionLevel(): Int = ZstdCompressionLevels.DEFAULT
		override fun inspectCompressionLevel(level: Int): CompressionLevelResult {
			return if (level in ZstdCompressionLevels.RANGE) {
				CompressionLevelResult.Valid
			} else {
				CompressionLevelResult.Invalid(ZstdCompressionLevels.MIN, ZstdCompressionLevels.MAX, ZstdCompressionLevels.DEFAULT)
			}
		}
	},

	/**
	 * Compress as a ZIP file, using DEFLATE, rather than a .tar.ext archive.
	 * Uses the `.zip` extension.
	 * @see <a href="https://en.wikipedia.org/wiki/Deflate">Wikipedia: DEFLATE</a>
	 * @see <a href="https://commons.apache.org/proper/commons-compress/apidocs/org/apache/commons/compress/archivers/zip/ZipArchiveOutputStream.html">Apache Commons Compress: ZipArchiveOutputStream</a>
	 */
	@SerialName("zip")
	ZIP {
		override fun getExtension(): String = "zip"
		override fun getOutputStream(input: OutputStream): OutputStream = throw NotImplementedError("This should NOT happen!")
		override fun defaultCompressionLevel(): Int = -1
		override fun inspectCompressionLevel(level: Int): CompressionLevelResult = CompressionLevelResult.Unused
	};

	/**
	 * Get the file extension for this compression algorithm.
	 * @return The file extension.
	 */
	abstract fun getExtension(): String

	/**
	 * Get an [OutputStream] that compresses the data written to it.
	 * @param input The [OutputStream] to compress.
	 * @return The compressed [OutputStream].
	 */
	abstract fun getOutputStream(input: OutputStream): OutputStream

	/**
	 * Get the default compression level for this compression algorithm.
	 * @return The default compression level.
	 */
	abstract fun defaultCompressionLevel(): Int

	/**
	 * Inspect a compression level to see if it is valid.
	 * @param level The compression level to inspect.
	 * @return A [CompressionLevelResult] indicating whether the compression level is valid, invalid, or unused.
	 */
	abstract fun inspectCompressionLevel(level: Int): CompressionLevelResult
}