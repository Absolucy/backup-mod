architectury {
	common(rootProject.property("enabled_platforms").toString().split(","))
}

loom {
	accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

dependencies {
	val versionInfo: MinecraftVersion by rootProject.extra
	val minecraftVersion: String by rootProject.extra

	modImplementation("net.fabricmc:fabric-loader:${versionInfo.fabric.loaderVersion}")
	modApi("net.fabricmc.fabric-api:fabric-api:${versionInfo.fabric.apiVersion}+${minecraftVersion}")
	// Remove the next line if you don't want to depend on the API
	modApi("dev.architectury:architectury-fabric:${versionInfo.architecturyVersion}")

	compileOnly(project(":common", "namedElements")) {
		isTransitive = false
	}
}

tasks.getByName("shrinkJar").enabled = false