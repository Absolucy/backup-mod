package moe.absolucy.backup.common

import net.minecraft.DefaultUncaughtExceptionHandler
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import net.minecraft.world.level.storage.LevelStorageSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class BackupThread(private val server: MinecraftServer, private val storageAccess: LevelStorageSource.LevelStorageAccess) : Thread() {

	var LOGGER: Logger = LoggerFactory.getLogger(BackupThread::class.java)

	init {
		// Ensure our priority is low, so we don't lag the server or anything
		priority = MIN_PRIORITY
		name = "Lucy's Backup Mod"
		uncaughtExceptionHandler = DefaultUncaughtExceptionHandler(LOGGER)
	}

	override fun run() {
		// TODO: run
	}

	private fun backup() {
		storageAccess.checkLock()
		val levelName = storageAccess.levelId
		val levelPath = storageAccess.getLevelPath(LevelResource.ROOT)
	}
}