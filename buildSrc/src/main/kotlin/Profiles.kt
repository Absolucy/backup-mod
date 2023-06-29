/**
 * The dependencies needed for the Minecraft Forge version of this mod.
 */
data class ForgeInfo(
		/**
		 * The version of Minecraft Forge to compile against.
		 * @see <a href="https://files.minecraftforge.net/net/minecraftforge/forge">Minecraft Forge downloads</a>
		 */
		val version: String,
		/**
		 * The version of Kotlin for Forge to compile against.
		 * @see <a href="https://modrinth.com/mod/kotlin-for-forge/versions">Kotlin for Forge on Modrinth</a>
		 */
		val kotlinVersion: String
)

/**
 * The dependencies needed for the Fabric version of this mod.
 */
data class FabricInfo(
		/**
		 * The version of Fabric Loader to compile against.
		 * @see <a href="https://fabricmc.net/develop">Fabric version utility</a>
		 */
		val loaderVersion: String,
		/**
		 * The version of the Fabric API to compile against.
		 * @see <a href="https://fabricmc.net/develop">Fabric version utility</a>
		 */
		val apiVersion: String,
		/**
		 * The version of the Fabric Language Kotlin extensions to compile against.
		 * @see <a href="https://modrinth.com/mod/fabric-language-kotlin/versions">Fabric Language Kotlin on Modrinth</a>
		 */
		val kotlinVersion: String
)

/**
 * The dependencies needed for the Quilt version of this mod.
 */
data class QuiltInfo(
		/**
		 * The version of the Quilt Loader to compile against.
		 * @see <a href="https://lambdaurora.dev/tools/import_quilt.html">Quilt Import Utility</a>
		 */
		val loaderVersion: String,
		/**
		 * The version of the Quilted Fabric API to compile against.
		 * @see <a href="https://lambdaurora.dev/tools/import_quilt.html">Quilt Import Utility</a>
		 */
		val fabricApiVersion: String,
		/**
		 * The version of the Quilt Standard Library to compile against.
		 * @see <a href="https://lambdaurora.dev/tools/import_quilt.html">Quilt Import Utility</a>
		 */
		val stdlibVersion: String,
		/**
		 * The version of the Quilt Kotlin libraries to compile against.
		 * @see <a href="https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-kotlin-libraries/quilt-kotlin-libraries">Quilt Kotlin libraries on the Quilt Maven repository</a>
		 */
		val kotlinVersion: String
)

/**
 * The dependencies needed to compile against a specific version of Minecraft.
 * This is used to determine which dependencies to add to the `build.gradle.kts` file.
 */
data class MinecraftVersion(
		/**
		 * The version of the Architectury API to compile against.
		 * @see <a href="https://modrinth.com/mod/architectury-api/versions">Architectury API on Modrinth</a>
		 */
		val architecturyVersion: String,
		/**
		 * The version of the Parchment mappings to use.
		 * @see <a href="https://ldtteam.jfrog.io/ui/native/parchmentmc-public/org/parchmentmc/data">ParchmentMC Repository</a>
		 */
		val parchmentVersion: String,
		/**
		 * Information about dependencies related to Minecraft Forge.
		 * @see [ForgeInfo]
		 */
		val forge: ForgeInfo,
		/**
		 * Information about dependencies related to Fabric.
		 * @see [FabricInfo]
		 */
		val fabric: FabricInfo,
		/**
		 * Information about dependencies related to Quilt.
		 * @see [QuiltInfo]
		 */
		val quilt: QuiltInfo
)

val minecraftVersions: Map<String, MinecraftVersion> = mapOf(
		"1.19.2" to MinecraftVersion(
				architecturyVersion = "6.5.85",
				parchmentVersion = "2022.11.27",
				forge = ForgeInfo(
						version = "1.19.2-43.2.0",
						kotlinVersion = "3.12.0"
				),
				fabric = FabricInfo(
						loaderVersion = "0.14.21",
						apiVersion = "0.76.0",
						kotlinVersion = "1.9.6+kotlin.1.8.22"
				),
				quilt = QuiltInfo(
						loaderVersion = "0.19.2-beta.2",
						fabricApiVersion = "4.0.0-beta.30+0.76.0",
						stdlibVersion = "3.0.0-beta.29",
						kotlinVersion = "2.1.0+kt.1.8.22+flk.1.9.4"
				)
		),
		"1.20.1" to MinecraftVersion(
				architecturyVersion = "9.0.8",
				parchmentVersion = "2023.06.26",
				forge = ForgeInfo(
						version = "1.20.1-47.0.35",
						kotlinVersion = "4.3.0"
				),
				fabric = FabricInfo(
						loaderVersion = "0.14.21",
						apiVersion = "0.84.0",
						kotlinVersion = "1.9.6+kotlin.1.8.22"
				),
				quilt = QuiltInfo(
						loaderVersion = "0.19.2-beta.2",
						fabricApiVersion = "7.0.4+0.84.0",
						stdlibVersion = "6.0.3",
						kotlinVersion = "2.1.0+kt.1.8.22+flk.1.9.4"
				)
		)

)