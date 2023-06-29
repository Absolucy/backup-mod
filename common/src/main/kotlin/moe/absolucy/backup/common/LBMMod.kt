package moe.absolucy.backup.common

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlIndentation
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import kotlinx.serialization.serializer
import moe.absolucy.backup.LBMExpectPlatform
import moe.absolucy.backup.common.config.LBMConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/** The main common class for Lucy's Backup Mod. */
object LBMMod {
	/** The ID of the mod. */
	const val MOD_ID = "lucys-backup-mod"

	/** The logger for the mod. */
	val LOGGER: Logger = LogManager.getLogger("Lucy's Backup Mod")

	/**
	 * The TOML serializer used by the mod.
	 */
	private val TOML = Toml(
			inputConfig = TomlInputConfig(
					ignoreUnknownNames = true,
			),
			outputConfig = TomlOutputConfig(
					indentation = TomlIndentation.TAB,
			)
	)

	/** The base configuration file path for the mod. */
	private val CONFIG_PATH: Path = LBMExpectPlatform.getConfigDirectory().resolve("$MOD_ID.toml")

	/**
	 * The configuration for the mod. Loaded from `config/lucys-backup-mod.toml` during
	 * initialization.
	 * @see [LBMConfig]
	 * @see [CONFIG_PATH]
	 * @see [loadConfig]
	 */
	var CONFIG = LBMConfig()

	/**
	 * Fetch the default configuration file from the JAR and write it to the config directory.
	 * @see [LBMConfig]
	 * @see [CONFIG_PATH]
	 */
	private fun writeDefaultConfig() {
		LBMMod::class.java.getResourceAsStream("/default-config.toml")?.also { defaultConfig ->
			try {
				defaultConfig.use { CONFIG_PATH.writeText(it.bufferedReader().readText()) }
				LOGGER.info("Wrote default configuration file to $CONFIG_PATH")
			} catch (e: Exception) {
				LOGGER.error("Failed to write default configuration file to '$CONFIG_PATH'", e)
			}
		} ?: run {
			LOGGER.error("Failed to load default configuration file from JAR! Using built-in default configuration.")
		}
	}

	/**
	 * Read the configuration file from the config directory.
	 * @see [LBMConfig]
	 * @see [CONFIG_PATH]
	 */
	private fun readConfig() {
		try {
			CONFIG = TOML.decodeFromString(serializer(), CONFIG_PATH.toFile().readText())
			LOGGER.info("Configuration file loaded successfully!: $CONFIG")
		} catch (e: Exception) {
			LOGGER.error("Failed to read configuration file! Using built-in default configuration.", e)
		}
	}

	/**
	 * Load the configuration file from the config directory, or create a default one if it doesn't
	 * exist.
	 * @see [LBMConfig]
	 * @see [CONFIG_PATH]
	 * @see [writeDefaultConfig]
	 * @see [readConfig]
	 */
	private fun loadConfig() {
		if (!CONFIG_PATH.exists()) {
			LOGGER.info("Configuration file not found, creating a default config!")
			writeDefaultConfig()
		}
		readConfig()
	}

	/**
	 * Initialize the common features of the mod, i.e. non-modloader specific features.
	 */
	fun init() {
		LOGGER.info("Lucy's Backup Mod initializing :3")
		loadConfig()
	}
}
