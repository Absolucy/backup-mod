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
	loader("quilt")
}

loom {
	accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentQuilt: Configuration by configurations.getting

configurations {
	compileOnly.configure { extendsFrom(common) }
	runtimeOnly.configure { extendsFrom(common) }
	developmentQuilt.extendsFrom(common)
}

dependencies {
	val versionInfo: MinecraftVersion by rootProject.extra
	val minecraftVersion: String by rootProject.extra

	modImplementation("org.quiltmc:quilt-loader:${versionInfo.quilt.loaderVersion}")
	modApi("org.quiltmc.quilted-fabric-api:quilted-fabric-api:${versionInfo.quilt.fabricApiVersion}-${minecraftVersion}")
	// Remove the next few lines if you don't want to depend on the API
	modApi("dev.architectury:architectury-fabric:${versionInfo.architecturyVersion}") {
		// We must not pull Fabric Loader from Architectury Fabric
		exclude("net.fabricmc")
		exclude("net.fabricmc.fabric-api")
	}
	modApi("org.quiltmc:qsl:${versionInfo.quilt.stdlibVersion}+${minecraftVersion}")

	common(project(":common", "namedElements")) {
		isTransitive = false
	}
	shadowCommon(project(":common", "transformProductionQuilt")) {
		isTransitive = false
	}
	common(project(":fabric-like", "namedElements")) {
		isTransitive = false
	}
	shadowCommon(project(":fabric-like", "transformProductionQuilt")) {
		isTransitive = false
	}

	modApi("org.quiltmc.quilt-kotlin-libraries:quilt-kotlin-libraries:${versionInfo.quilt.kotlinVersion}")
}

tasks.shadowJar {
	exclude("architectury.common.json")
	configurations = listOf(shadowCommon)

	// Include the shadowed JAR of the common project
	val commonProject = project(":common")
	val commonShadowJar = commonProject.tasks.getByName<ShadowJar>("shadowJar")
	from(commonShadowJar.archiveFile.map { zipTree(it) })

	// Make sure to run the common:shadowJar task before this task
	dependsOn(commonShadowJar)
	archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
	//injectAccessWidener.set(true)
	inputFile.set(tasks.shadowJar.get().archiveFile)
	dependsOn(tasks.shadowJar)
	archiveClassifier.set("slim")
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