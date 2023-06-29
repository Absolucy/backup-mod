package moe.absolucy.backup.quilt

import moe.absolucy.backup.fabriclike.LBMModFabricLike
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

class LBMModQuilt : ModInitializer {
	override fun onInitialize(mod: ModContainer?) {
		LBMModFabricLike.init()
	}
}