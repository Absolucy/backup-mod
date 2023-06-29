import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("com.github.johnrengelman.shadow")
}

repositories {
	maven {
		url = uri("https://maven.quiltmc.org/repository/release/")
	}
}

architectury {
	platformSetupLoomIde()
	fabric()
}

loom {
	accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentFabric: Configuration by configurations.getting

configurations {
	compileOnly.configure { extendsFrom(common) }
	runtimeOnly.configure { extendsFrom(common) }
	developmentFabric.extendsFrom(common)
}

dependencies {
	val versionInfo: MinecraftVersion by rootProject.extra
	val minecraftVersion: String by rootProject.extra

	modImplementation("net.fabricmc:fabric-loader:${versionInfo.fabric.loaderVersion}")
	modApi("net.fabricmc.fabric-api:fabric-api:${versionInfo.fabric.apiVersion}+${minecraftVersion}")
	// Remove the next line if you don't want to depend on the API
	modApi("dev.architectury:architectury-fabric:${versionInfo.architecturyVersion}")

	common(project(":common", "namedElements")) {
		isTransitive = false
	}
	shadowCommon(project(":common", "transformProductionFabric")) {
		isTransitive = false
	}
	common(project(":fabric-like", "namedElements")) {
		isTransitive = false
	}
	shadowCommon(project(":fabric-like", "transformProductionFabric")) {
		isTransitive = false
	}

	// Fabric Kotlin
	modImplementation("net.fabricmc:fabric-language-kotlin:${versionInfo.fabric.kotlinVersion}")
}

tasks.shadowJar {
	exclude("architectury.common.json")
	configurations = listOf(shadowCommon)

	// Include the shadowed JAR of the common project
	val commonProject = project(":common")
	val commonShadowJar = commonProject.tasks.getByName<ShadowJar>("shadowJar")

	// Make sure to run the common:shadowJar task before this task
	dependsOn(commonShadowJar)
	archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
	//injectAccessWidener.set(true)
	inputFile.set(tasks.shadowJar.get().archiveFile)
	dependsOn(tasks.shadowJar)
	archiveClassifier.set(null as String?)
}

tasks.jar {
	archiveClassifier.set("dev")
}

tasks.sourcesJar {
	val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
	dependsOn(commonSources)
	from(commonSources.archiveFile.map { zipTree(it) })
}

components.getByName("java") {
	this as AdhocComponentWithVariants
	this.withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
		skip()
	}
}