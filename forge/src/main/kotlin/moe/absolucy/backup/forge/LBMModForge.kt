package moe.absolucy.backup.forge

import dev.architectury.platform.forge.EventBuses
import moe.absolucy.backup.common.LBMMod
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(LBMMod.MOD_ID)
class LBMModForge {
	init {
		// Submit our event bus to let architectury register our content on the right time
		EventBuses.registerModEventBus(LBMMod.MOD_ID, MOD_BUS)
		LBMMod.init()
	}
}