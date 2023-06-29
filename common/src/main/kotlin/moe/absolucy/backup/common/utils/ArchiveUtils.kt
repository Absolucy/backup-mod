package moe.absolucy.backup.common.utils

import com.appmattus.crypto.Algorithm
import moe.absolucy.backup.common.LBMMod
import moe.absolucy.backup.common.config.CompressionAlgorithm
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.sevenz.SevenZMethod
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.utils.CountingInputStream
import org.tukaani.xz.LZMA2Options
import java.io.File
import java.io.OutputStream
import java.nio.file.Path

internal data class ArchiveResult(val size: ULong, val checksum: ByteArray) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ArchiveResult

		if (size != other.size) return false
		return checksum.contentEquals(other.checksum)
	}

	override fun hashCode(): Int {
		var result = size.hashCode()
		result = 31 * result + checksum.contentHashCode()
		return result
	}
}


internal object ArchiveUtils {
	/**
	 * Creates an archive containing the contents of the specified directory.
	 * @param directory The directory to archive.
	 * @param to The path to write the newly created archive to.
	 * @return A result containing the size of the archives contents, in bytes, and a checksum of the archive.
	 */
	fun createArchive(directory: Path, to: Path): ArchiveResult {
		val directoryFile = directory.toFile()
		val compressionAlgorithm = LBMMod.CONFIG.compressionAlgorithm
		val toFile = to.toFile().apply { createNewFile() }
		return when (compressionAlgorithm) {
			CompressionAlgorithm.SEVENZ -> {
				val bytesCopied = create7zArchive(directoryFile, toFile)
				// Unfortunately, we have to read the file again to calculate the digest.
				val digest = toFile.inputStream().buffered().use {
					val digestStream = DigestOutputStream(Algorithm.XXH3_64())
					it.copyTo(digestStream)
					digestStream.digest()
				}
				ArchiveResult(bytesCopied, digest)
			}

			CompressionAlgorithm.ZIP -> {
				DigestOutputStream(Algorithm.XXH3_64(), toFile.outputStream().buffered()).use {
					val bytesCopied = createZipArchive(directoryFile, it)
					val digest = it.digest()
					ArchiveResult(bytesCopied, digest)
				}
			}

			else -> {
				DigestOutputStream(Algorithm.XXH3_64(), toFile.outputStream().buffered()).use {
					val bytesCopied = createTarArchive(directoryFile, it)
					val digest = it.digest()
					ArchiveResult(bytesCopied, digest)
				}
			}
		}
	}

	/**
	 * Creates a TAR archive of the specified directory, optionally compressing it.
	 * @param directoryFile The directory to archive.
	 * @param fileOutputStream The output stream to write the archive to.
	 * @return The sum of the sizes of all files written to the archive, in bytes.
	 */
	private fun createTarArchive(directoryFile: File, fileOutputStream: OutputStream): ULong {
		return LBMMod.CONFIG.compressor(fileOutputStream).use { compressedOutputStream ->
			TarArchiveOutputStream(compressedOutputStream).apply {
				setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
				setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX)
			}.use { walkDirectoryIntoArchive(directoryFile, it) }
		}
	}

	/**
	 * Creates a 7z archive of the specified directory, compressed with LZMA2.
	 * @param directoryFile The directory to archive.
	 * @param file The file to write the archive to.
	 * @return The sum of the sizes of all files written to the archive, in bytes.
	 */
	private fun create7zArchive(directoryFile: File, file: File): ULong {
		return SevenZOutputFile(file).apply {
			setContentMethods(listOf(SevenZMethodConfiguration(SevenZMethod.LZMA2, LZMA2Options(LBMMod.CONFIG.compressionLevel))))
		}.use { sevenZOutputFile ->
			directoryFile.walk().filter { it.isFile }.map { createAndCopy7zEntry(it, directoryFile, sevenZOutputFile) }.sum()
		}
	}


	/**
	 * Creates a ZIP archive of the specified directory.
	 * @param directoryFile The directory to archive.
	 * @param fileOutputStream The output stream to write the archive to.
	 * @return The sum of the sizes of all files written to the archive, in bytes.
	 */
	private fun createZipArchive(directoryFile: File, fileOutputStream: OutputStream): ULong {
		return ZipArchiveOutputStream(fileOutputStream).use { walkDirectoryIntoArchive(directoryFile, it) }
	}

	/**
	 * Walks the specified directory and writes all files to the specified archive output stream.
	 * @param directoryFile The directory to walk.
	 * @param archiveOutputStream The archive output stream to write files to.
	 * @return The sum of the sizes of all files written to the archive output stream, in bytes.
	 */
	private fun walkDirectoryIntoArchive(directoryFile: File, archiveOutputStream: ArchiveOutputStream): ULong {
		return directoryFile.walk().filter { it.isFile }.map { createAndCopyArchiveEntry(it, directoryFile, archiveOutputStream) }.sum()
	}

	/**
	 * Creates an archive entry for a file and copies the file to the archive output stream.
	 * @param file The file to create an archive entry for and copy.
	 * @param directoryFile The directory containing the file, used to compute the relative path.
	 * @param archiveOutputStream The tar output stream to copy the file to.
	 * @return The size of the file written to the archive output stream, in bytes.
	 */
	private fun createAndCopyArchiveEntry(file: File, directoryFile: File, archiveOutputStream: ArchiveOutputStream): ULong {
		return file.relativeTo(directoryFile).toString().let { relativePath ->
			archiveOutputStream.createArchiveEntry(file, relativePath).let { entry ->
				archiveOutputStream.putArchiveEntry(entry)
				file.inputStream().buffered().use { it.copyTo(archiveOutputStream).toULong() }.also {
					archiveOutputStream.closeArchiveEntry()
				}
			}
		}
	}

	/**
	 * Creates an archive entry for a file and copies the file to the 7z output stream.
	 * @param file The file to create an archive entry for and copy.
	 * @param directoryFile The directory containing the file, used to compute the relative path.
	 * @param sevenZOutputFile The 7z output stream to copy the file to.
	 * @return The size of the file written to the archive output stream, in bytes.
	 */
	private fun createAndCopy7zEntry(file: File, directoryFile: File, sevenZOutputFile: SevenZOutputFile): ULong {
		return file.relativeTo(directoryFile).toString().let { relativePath ->
			sevenZOutputFile.putArchiveEntry(sevenZOutputFile.createArchiveEntry(file, relativePath))
			file.inputStream().buffered().use { bufferedInputStream ->
				CountingInputStream(bufferedInputStream).use { sevenZOutputFile.write(it); it.bytesRead.toULong() }
			}.also { sevenZOutputFile.closeArchiveEntry() }
		}
	}

}