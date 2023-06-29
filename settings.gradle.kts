pluginManagement {
	repositories {

		maven(url = "https://maven.fabricmc.net/")
		maven(url = "https://maven.architectury.dev/")
		maven(url = "https://maven.minecraftforge.net/")
		gradlePluginPortal()
	}
}

include("common")
include("fabric-like")
include("fabric")
include("quilt")
include("forge")

rootProject.name = "lucys-backup-mod"
