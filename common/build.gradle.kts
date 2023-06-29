import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.architectury.plugin.TransformingTask

plugins {
	kotlin("plugin.serialization")
	id("com.github.johnrengelman.shadow")
}

architectury {
	common(rootProject.property("enabled_platforms").toString().split(","))
}

// Create a new configuration for the library

loom {
	accessWidenerPath.set(file("src/main/resources/moe.absolucy.backup.accesswidener"))
}

val shadowConfig: Configuration by configurations.creating

dependencies {
	val versionInfo: MinecraftVersion by rootProject.extra
	val compressVersion =
			rootProject.properties["apache-commons-compress-version"]?.toString()
					?: DefaultProperties.DEPENDENCIES.apacheCommonsCompress
	val textVersion =
			rootProject.properties["apache-commons-text-version"]?.toString()
					?: DefaultProperties.DEPENDENCIES.apacheCommonsText
	val vcdiffVersion =
			rootProject.properties["vcdiff-version"]?.toString()
					?: DefaultProperties.DEPENDENCIES.vcdiff
	val kotlinxVersion =
			rootProject.properties["kotlinx-version"]?.toString()
					?: DefaultProperties.DEPENDENCIES.kotlinx
	val okioVersion =
			rootProject.properties["okio-version"]?.toString()
					?: DefaultProperties.DEPENDENCIES.okio
	val ktomlVersion =
			rootProject.properties["ktoml-version"]?.toString()
					?: DefaultProperties.DEPENDENCIES.ktoml
	val cryptohashVersion =
			rootProject.properties["cryptohash-version"]?.toString()
					?: DefaultProperties.DEPENDENCIES.cryptohash
	var zstdJniVersion =
			rootProject.properties["zstd-jni-version"]?.toString()
					?: DefaultProperties.DEPENDENCIES.zstd
	var xzVersion =
			rootProject.properties["xz-version"]?.toString()
					?: DefaultProperties.DEPENDENCIES.xz

	modImplementation("net.fabricmc:fabric-loader:${versionInfo.fabric.loaderVersion}")
	modApi("dev.architectury:architectury:${versionInfo.architecturyVersion}")

	// These two are not shadowed, as they are typically included in the Kotlin runtime provided by the mod dependencies.
	compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxVersion")
	compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxVersion")

	for (dependency in listOf(
			"com.akuleshov7:ktoml-core-jvm:$ktomlVersion",
			"com.akuleshov7:ktoml-source-jvm:$ktomlVersion",
			"com.appmattus.crypto:cryptohash-jvm:$cryptohashVersion",
			"com.davidehrmann.vcdiff:vcdiff-core:$vcdiffVersion",
			"com.github.luben:zstd-jni:$zstdJniVersion",
			"org.apache.commons:commons-compress:$compressVersion",
			"org.apache.commons:commons-text:$textVersion",
			"org.tukaani:xz:$xzVersion"
	)) {
		compileOnly(dependency)
		shadowConfig(dependency) {
			isTransitive = false
			exclude("com.sun.jna")
		}
	}

	// We don't even USE these ourselves - they're dependencies of the previously shadowed dependencies.
	shadowConfig("com.squareup.okio:okio-jvm:$okioVersion") {
		isTransitive = false
		exclude("com.sun.jna")
	}
}

tasks {
	val shadowJar by getting(ShadowJar::class) {
		val basePackage: String by rootProject.extra
		configurations = listOf(shadowConfig)
		relocate("com.akuleshov7", "$basePackage.com.akuleshov7")
		relocate("com.appmattus.crypto", "$basePackage.com.appmattus.crypto")
		relocate("com.davidehrmann", "$basePackage.com.davidehrmann")
		relocate("com.github.luben", "$basePackage.com.github.luben")
		relocate("com.github.shyiko", "$basePackage.com.github.shyiko")
		relocate("okio", "$basePackage.okio")
		relocate("org.apache.commons", "$basePackage.org.apache.commons")
		relocate("org.tukaani", "$basePackage.org.tukaani")
	}

	withType<TransformingTask> {
		input.set(shadowJar.archiveFile)
	}
}

tasks.getByName("shrinkJar").enabled = false
