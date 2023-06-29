package moe.absolucy.backup.common.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.absolucy.backup.common.LBMMod
import moe.absolucy.backup.common.schedule.Schedule
import java.io.OutputStream
import java.time.ZonedDateTime

typealias ScheduleList = @Serializable(with = ScheduleListSerializer::class) List<Schedule>

/**
 * The configuration file for Lucy's Backup Mod.
 * Located in `config/lucys-backup-mod.toml`.
 */
@Serializable
data class LBMConfig(
		/**
		 * Whether backups are enabled or not.
		 */
		val enabled: Boolean = true,
		/**
		 * The directory, relative to the base server/minecraft folder, to back up to.
		 * If this is not set, the default is to use the 'backups' folder.
		 */
		@SerialName("backup-directory")
		val backupDirectory: String = "backups",
		/**
		 * Whether to put every backup and its metadata in a subfolder of the backup directory,
		 * or to put them all in the root of the backup directory.
		 */
		@SerialName("use-subfolders")
		val useSubfolders: Boolean = true,
		/**
		 * Which compression method to use.
		 * @see [CompressionAlgorithm]
		 */
		@SerialName("compression-algorithm")
		var compressionAlgorithm: CompressionAlgorithm = CompressionAlgorithm.ZSTANDARD,
		/**
		 * The compression level to use.
		 * Setting to -1 will use the default compression level for the given compression algorithm.
		 */
		@SerialName("compression-level")
		@Serializable(with = LongToIntSerializer::class)
		var compressionLevel: Int = -1,
		/**
		 * Schedules in which to run backups.
		 * These use the GAE cron format.
		 * @see <a href="https://cloud.google.com/appengine/docs/legacy/standard/java/config/cronref#example">GAE cron format documentation</a>
		 */
		@SerialName("backup-schedule")
		val backupSchedule: ScheduleList = listOf(),
		/**
		 * The number of backups to keep.
		 * When the number of backups exceeds this number, the oldest backups will be deleted.
		 * If this is set to 0, all backups will be kept.
		 */
		@SerialName("backups-to-keep")
		@Serializable(with = LongToIntSerializer::class)
		val backupsToKeep: Int = 10,
) {
	init {
		// Runs config validation.
		validate()
	}

	/**
	 * Validates the config.
	 */
	private fun validate() {
		validateBackupDirectory()
		validateCompressionLevel()
	}

	/**
	 * Validates the backup directory, and throws an exception if it is invalid.
	 */
	private fun validateBackupDirectory() {
		if (backupDirectory.contains("..")) {
			throw IllegalArgumentException("Backup directory cannot contain '..'")
		}
	}

	/**
	 * Validates the compression level, and sets it to the default if it is invalid.
	 */
	private fun validateCompressionLevel() {
		if (compressionLevel == -1) {
			compressionLevel = compressionAlgorithm.defaultCompressionLevel()
			return
		}
		when (val compressionLevelQuery = compressionAlgorithm.inspectCompressionLevel(compressionLevel)) {
			is CompressionLevelResult.Invalid -> {
				val min = compressionLevelQuery.min
				val max = compressionLevelQuery.max
				val default = compressionLevelQuery.default
				LBMMod.LOGGER.error("Compression level $compressionLevel is invalid for ${compressionAlgorithm.name}; " +
						"must be between $min and $max, inclusive. Using default value of $default instead.")
				compressionLevel = default
			}

			is CompressionLevelResult.Unused -> {
				if (compressionLevel > 0) {
					LBMMod.LOGGER.warn("Compression level $compressionLevel is not used by ${compressionAlgorithm.name}.")
				}
			}

			else -> {}
		}
	}

	/**
	 * Get a compressor for the given output stream, using the compression algorithm specified in the config.
	 * @param out The output stream to compress.
	 * @return The compressor for the given output stream.
	 */
	fun compressor(out: OutputStream) = compressionAlgorithm.getOutputStream(out)

	/**
	 * Get the next backup time, or null if there are no scheduled backups.
	 * @return The next backup time, or null if there are no scheduled backups.
	 */
	fun nextBackupTime(): ZonedDateTime? {
		val now = ZonedDateTime.now()
		return backupSchedule.minOfOrNull { it.nextOrSame(now) }
	}
}

/**
 * A serializer for [ScheduleList].
 */
private object ScheduleListSerializer : KSerializer<ScheduleList> {
	override val descriptor = String.serializer().descriptor

	override fun serialize(encoder: Encoder, value: ScheduleList) {
		val scheduleStringList = value.map { it.toString() }
		encoder.encodeSerializableValue(ListSerializer(String.serializer()), scheduleStringList)
	}

	override fun deserialize(decoder: Decoder): ScheduleList {
		val scheduleStringList = decoder.decodeSerializableValue(ListSerializer(String.serializer()))
		return scheduleStringList.map { Schedule.parse(it) }
	}
}

/**
 * A serializer that serializes a [Long] to an [Int].
 * This is used as a workaround for ktoml's lack of support for [Int]s, so I don't have to sprinkle toLong() calls everywhere.
 */
private object LongToIntSerializer : KSerializer<Int> {
	override val descriptor = Long.serializer().descriptor

	override fun serialize(encoder: Encoder, value: Int) {
		encoder.encodeLong(value.toLong())
	}

	override fun deserialize(decoder: Decoder): Int = decoder.decodeLong().toInt()
}