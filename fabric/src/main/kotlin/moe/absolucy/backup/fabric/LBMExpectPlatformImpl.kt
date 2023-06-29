package moe.absolucy.backup.fabric

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

object LBMExpectPlatformImpl {
	@JvmStatic
	fun getConfigDirectory(): Path {
		return FabricLoader.getInstance().configDir
	}
}