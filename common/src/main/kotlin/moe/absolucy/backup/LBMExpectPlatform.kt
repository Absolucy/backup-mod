package moe.absolucy.backup

import dev.architectury.injectables.annotations.ExpectPlatform
import java.nio.file.Path

object LBMExpectPlatform {
	@JvmStatic
	@ExpectPlatform
	fun getConfigDirectory(): Path {
		// Just throw an error, the content should get replaced at runtime.
		throw AssertionError()
	}
}