package moe.absolucy.backup.fabric

import moe.absolucy.backup.fabriclike.LBMModFabricLike
import net.fabricmc.api.ModInitializer

class LBMModFabric : ModInitializer {
	override fun onInitialize() {
		LBMModFabricLike.init()
	}
}
