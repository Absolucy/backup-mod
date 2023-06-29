package moe.absolucy.backup.forge

import net.minecraftforge.fml.loading.FMLPaths
import java.nio.file.Path

object LBMExpectPlatformImpl {
	@JvmStatic
	fun getConfigDirectory(): Path {
		return FMLPaths.CONFIGDIR.get()
	}
}