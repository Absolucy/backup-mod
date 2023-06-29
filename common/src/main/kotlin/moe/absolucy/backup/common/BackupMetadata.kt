package moe.absolucy.backup.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Metadata about a single world backup.
 * This is stored in the world backup directory as a JSON file,
 * with the same name as the world backup file, but with the extension ".metadata.json".
 */
@Serializable
data class BackupMetadata(
		/**
		 * The ID of the world being backed up.
		 */
		@SerialName("world-id")
		val worldId: String,
		/**
		 * The compression method used to compress the world backup.
		 * @see <a href="https://commons.apache.org/proper/commons-compress/apidocs/org/apache/commons/compress/compressors/CompressorStreamFactory.html">Apache Commons Compress: CompressorStreamFactory</a>
		 */
		@SerialName("compression-method")
		val compressionMethod: String,
		/**
		 * The size of the uncompressed world backup, in bytes.
		 */
		@SerialName("uncompressed-size")
		val uncompressedSize: ULong,
		/**
		 * The hash of the world backup, calculated using XXHash3-64.
		 * A cryptographic hash is not used because it is not necessary for this use case,
		 * as we only care about file corruption, not malicious tampering.
		 * @see <a href="https://xxhash.com">xxhash.com</a>
		 */
		@SerialName("hash")
		val hash: String,
		/**
		 * The time the backup was created, in seconds since the Unix epoch.
		 * This is used to determine which backups are the oldest and should be deleted first.
		 */
		@SerialName("creation-time")
		val creationTime: Long = Instant.now().epochSecond
)