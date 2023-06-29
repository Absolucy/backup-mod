import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("com.github.johnrengelman.shadow")
}

architectury {
	platformSetupLoomIde()
	forge()
}

loom {
	accessWidenerPath.set(project(":common").loom.accessWidenerPath)

	forge.apply {
		convertAccessWideners.set(true)
		extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)

		mixinConfig("moe.absolucy.backup.common.mixins.json")
		mixinConfig("moe.absolucy.backup.mixins.json")
	}
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentForge: Configuration by configurations.getting

configurations {
	compileOnly.configure { extendsFrom(common) }
	runtimeOnly.configure { extendsFrom(common) }
	developmentForge.extendsFrom(common)
}

repositories {
	// KFF
	maven {
		name = "Kotlin for Forge"
		setUrl("https://thedarkcolour.github.io/KotlinForForge/")
	}
}

dependencies {
	val versionInfo: MinecraftVersion by rootProject.extra

	forge("net.minecraftforge:forge:${versionInfo.forge.version}")
	// Remove the next line if you don't want to depend on the API
	modApi("dev.architectury:architectury-forge:${versionInfo.architecturyVersion}")

	common(project(":common", "namedElements")) { isTransitive = false }
	shadowCommon(project(":common", "transformProductionForge")) { isTransitive = false }

	// Kotlin For Forge
	implementation("thedarkcolour:kotlinforforge:${versionInfo.forge.kotlinVersion}")
}

tasks.shadowJar {
	exclude("fabric.mod.json")
	exclude("architectury.common.json")
	configurations = listOf(shadowCommon)

	// Include the shadowed JAR of the common project
	val commonProject = project(":common")
	val commonShadowJar = commonProject.tasks.getByName<ShadowJar>("shadowJar")
	from(commonShadowJar.archiveFile.map { zipTree(it) })

	// Make sure to run the common:shadowJar task before this task
	dependsOn(commonShadowJar)
}

tasks.remapJar {
	//injectAccessWidener.set(true)
	inputFile.set(tasks.shadowJar.get().archiveFile)
	dependsOn(tasks.shadowJar)
	archiveClassifier.set(null as String?)
}

tasks.jar {
	archiveClassifier.set("slim")
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