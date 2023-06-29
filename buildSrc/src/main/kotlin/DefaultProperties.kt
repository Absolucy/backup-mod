/**
 * Default properties for the build, used when fields are not specified in gradle.properties.
 */
object DefaultProperties {
	/**
	 * The base name of the archives.
	 * Maps to `archives_base_name` in gradle.properties.
	 */
	const val ARCHIVES_BASE_NAME = "lucys-backup-mod"

	/**
	 * The mod ID.
	 * Maps to `mod_id` in gradle.properties.
	 */
	const val MOD_ID = ARCHIVES_BASE_NAME

	/**
	 * The version of the mod.
	 * Maps to `mod_version` in gradle.properties.
	 */
	const val MOD_VERSION = "0.1.0"

	/**
	 * The version of Minecraft to compile against.
	 * Maps to `minecraft_version` in gradle.properties.
	 */
	const val MC_VERSION = "1.19.2"

	/**
	 * The maven group of the mod.
	 * Maps to `maven_group` in gradle.properties.
	 */
	const val MAVEN_GROUP = "moe.absolucy"

	/**
	 * The base package of the mod.
	 * Maps to `base_package` in gradle.properties.
	 */
	const val BASE_PACKAGE = "$MAVEN_GROUP.backup"

	/**
	 * The version of Kotlin to use.
	 * Maps to `kotlin_version` in gradle.properties.
	 * @see <a href="https://kotlinlang.org/docs/releases.html#release-details">Kotlin release details</a>
	 */
	const val KOTLIN_VERSION = "1.8.22"

	/**
	 * The version of Java to use.
	 * Maps to `java_version` in gradle.properties.
	 */
	const val JAVA_VERSION = "17"

	/**
	 * Default versions for non-Minecraft library dependencies.
	 * These use the Gradle version syntax.
	 * @see <a href="https://docs.gradle.org/current/userguide/single_versions.html">Gradle user guide: Declaring Versions and Ranges</a>
	 */
	data class Dependencies(
			/**
			 * The version of Apache Commons Compress to use.
			 * Maps to `apache-commons-compress-version` in gradle.properties.
			 * @see <a href="https://commons.apache.org/proper/commons-compress/dependency-info.html">Apache Commons Compress: Dependency Information</a>
			 */
			val apacheCommonsCompress: String,
			/**
			 * The version of Apache Commons Text to use.
			 * Maps to `apache-commons-text-version` in gradle.properties.
			 * @see <a href="https://commons.apache.org/proper/commons-text/dependency-info.html">Apache Commons Text: Dependency Information</a>
			 */
			val apacheCommonsText: String,
			/**
			 * The version of the ProGuard Gradle plugin to use.
			 * Maps to `proguard-version` in gradle.properties.
			 * @see <a href="https://github.com/Guardsquare/proguard/releases">GitHub: ProGuard</a>
			 */
			val proguard: String,
			/**
			 * The version of the vcdiff-java library to use.
			 * Maps to `vcdiff-version` in gradle.properties.
			 * @see <a href="https://github.com/ehrmann/vcdiff-java/releases">GitHub: vcdiff-java</a>
			 */
			val vcdiff: String,
			/**
			 * The version of kotlinx.serialization and kotlinx.serialization.json to use.
			 * Maps to `kotlinx-version` in gradle.properties.
			 * @see <a href="https://github.com/Kotlin/kotlinx.serialization/releases">GitHub: kotlinx.serialization</a>
			 */
			var kotlinx: String,
			/**
			 * The version of okio to use.
			 * Maps to `okio-version` in gradle.properties.
			 * @see <a href="https://github.com/square/okio/tags">GitHub: okio</a>
			 */
			var okio: String,
			/**
			 * The version of ktoml to use.
			 * Maps to `ktoml-version` in gradle.properties.
			 * @see <a href="https://github.com/akuleshov7/ktoml/releases">GitHub: ktoml</a>
			 */
			var ktoml: String,
			/**
			 * The version of cryptohash to use.
			 * Maps to `cryptohash-version` in gradle.properties.
			 * @see <a href="https://github.com/appmattus/crypto/releases">GitHub: cryptohash</a>
			 */
			var cryptohash: String,
			/**
			 * The version of zstd-jni to use.
			 * Maps to `zstd-jni-version` in gradle.properties.
			 * @see <a href="https://github.com/luben/zstd-jni/tags">GitHub: zstd-jni</a>
			 */
			var zstd: String,
			/**
			 * The version of XZ for Java to use.
			 * Maps to `xz-version` in gradle.properties.
			 * @see <a href="https://tukaani.org/xz/java.html">XZ for Java</a>
			 */
			var xz: String,
	)

	val DEPENDENCIES = Dependencies(
			apacheCommonsCompress = "1.23.+",
			apacheCommonsText = "1.10.+",
			proguard = "7.3.+",
			vcdiff = "0.1.+",
			kotlinx = "1.5.+",
			okio = "3.3.+",
			ktoml = "0.5.+",
			cryptohash = "0.10.+",
			zstd = "1.5.+",
			xz = "1.9",
	)
}