import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import proguard.gradle.ProGuardTask

// Credit to https://gits.username404.fr/Username404-59/SnowyGUI/src/branch/1.20/build.gradle.kts for some of the stuff here,
// most notably the ProGuard optimization stuff, resource processing, jar remapping, and compiler tuning.

val javaVersion by extra {
	rootProject.properties["java_version"]?.toString() ?: DefaultProperties.JAVA_VERSION
}
val sourceJavaVersion: String = javaVersion
val kotlinVersion by extra {
	rootProject.properties["kotlin_version"]?.toString() ?: DefaultProperties.KOTLIN_VERSION
}
val kotlinSplitVersion = kotlinVersion.split('.')
val archivesBaseName by extra {
	rootProject.properties["archives_base_name"]?.toString() ?: DefaultProperties.ARCHIVES_BASE_NAME
}
val modId by extra { rootProject.properties["mod_id"]?.toString() ?: DefaultProperties.MOD_ID }
val modVersion by extra { rootProject.properties["mod_version"]?.toString() ?: DefaultProperties.MOD_VERSION }
val minecraftVersion by extra {
	rootProject.properties["minecraft_version"]?.toString() ?: DefaultProperties.MC_VERSION
}
val mavenGroup by extra { rootProject.properties["maven_group"]?.toString() ?: DefaultProperties.MAVEN_GROUP }
val basePackage by extra { rootProject.properties["base_package"]?.toString() ?: DefaultProperties.BASE_PACKAGE }
val versionInfo by extra {
	minecraftVersions[minecraftVersion]
			?: throw IllegalArgumentException("Minecraft version $minecraftVersion is not supported!")
}

version = modVersion
group = mavenGroup

buildscript {
	dependencies {
		val proguardVersion = rootProject.properties["proguard-version"]?.toString()
				?: DefaultProperties.DEPENDENCIES.proguard
		classpath("com.guardsquare:proguard-gradle:${proguardVersion}") {
			exclude("com.android.tools.build")
		}
	}
}

val jarsDir = File(rootDir, "jars")
var remappedJarsDir = File(jarsDir, "remapped")
var shrinkedJarsDir = File(jarsDir, "shrinked")

if (!remappedJarsDir.exists()) remappedJarsDir.mkdirs()
if (!shrinkedJarsDir.exists()) shrinkedJarsDir.mkdirs()

plugins {
	java
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.serialization") version "1.8.21" apply false
	id("architectury-plugin") version "3.4-SNAPSHOT"
	id("dev.architectury.loom") version "1.2-SNAPSHOT" apply false
	id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

architectury {
	minecraft = minecraftVersion
}

subprojects {
	apply(plugin = "dev.architectury.loom")
	apply(plugin = "com.github.johnrengelman.shadow")

	val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")
	val shadowC: Configuration by configurations.creating

	lateinit var mappingsDep: Dependency
	extensions.configure<LoomGradleExtension>("loom") {
		mappingsDep = layered {
			officialMojangMappings().parchment("org.parchmentmc.data:parchment-${minecraftVersion}:${versionInfo.parchmentVersion}")
		}
		silentMojangMappingsLicense()
	}

	repositories {
		mavenCentral()
		maven(url = "https://maven.parchmentmc.org")
	}

	dependencies {
		"minecraft"("com.mojang:minecraft:${minecraftVersion}")
		"mappings"(mappingsDep)
	}

	tasks {
		withType(ShadowJar::class) {
			this.configurations = listOf(shadowC)
			exclude("com/sun/jna/**/*")
			exclude("**/*.kotlin_metadata")
			exclude("**/*.kotlin_builtins")
			exclude("META-INF/maven/**/*")
			archiveClassifier.set("shadow-${this@subprojects.name}")
		}

		val remapJar = getByName("remapJar") as RemapJarTask
		val shrinkJar = register("shrinkJar", ProGuardTask::class) {
			group = this@subprojects.group as String
			dependsOn(remapJar)
			injars(remapJar)
			outjars(File(shrinkedJarsDir, "${archivesBaseName}-[v${modVersion}+${loom.platform.get().toString().lowercase()}+mc${minecraftVersion}].jar"))
			keep("class $basePackage.mixins.* { *; }")
			keep("class $basePackage.common.LBM* { *; }")
			for (subpath in listOf("forge", "fabric", "fabriclike", "quilt")) {
				keep("class $basePackage.$subpath.** { *; }")
			}
			keepattributes("*Annotation*,EnclosingMethod,InnerClasses,Signature,Exceptions,Synthetic,SourceFile,LineNumberTable,LocalVariable*Table,*Deprecated*,* synthetic *,*Bridge,MethodParameters,AnnotationDefault,Module*,NestHost,NestMembers,*Module*,Record")
			adaptclassstrings()
			dontwarn(); dontnote(); dontobfuscate()

			doFirst {
				libraryjars(configurations.compileClasspath.get().filterNot { file ->
					shadowC.contains(file)
				})
			}
			val homeDir = System.getProperty("java.home") as String
			val jmodsLocations = setOf(
					"$homeDir/jmods/java.base.jmod",
					"$homeDir/jmods/java.desktop.jmod",
					"$homeDir/jmods/java.instrument.jmod"
			)
			if (jmodsLocations.all { !file(it).exists() }) throw GradleException("Jmods appear to be missing, please make sure that jmods are installed.")
			else jmodsLocations.forEach {
				libraryjars(it)
			}
			// Note: dontpreverify() should NOT be used, it will cause errors at runtime
			optimizations("method/inlining/*, code/merging, code/removal/*, code/simplification/*")
			optimizationpasses(5)
		}

		if (this@subprojects.name != "common" && this@subprojects.name != "fabric-like") build.get().finalizedBy(shrinkJar)

		withType(ProcessResources::class) {
			with(project(":common").sourceSets.main.get().resources.srcDirs) {
				if (!sourceSets.main.get().resources.srcDirs.containsAll(this)) {
					from(this)
				}
			}
			val modProperties = mapOf(
					"version" to (rootProject.version as String),
					"mod_id" to modId,
					"minecraft_version" to minecraftVersion,
					"group" to mavenGroup,
					"fabric_kotlin_version" to versionInfo.fabric.kotlinVersion,
					"forge_kotlin_version" to versionInfo.forge.kotlinVersion,
					"fabric_loader" to versionInfo.fabric.loaderVersion,
					"quilt_loader" to versionInfo.quilt.loaderVersion,
					"forge_version" to versionInfo.forge.version,
					"architectury_version" to versionInfo.architecturyVersion
			)
			inputs.properties(modProperties)
			filesNotMatching(listOf("*.png", "*.accesswidener")) {
				expand(modProperties)
			}
		}
		check {
			setDependsOn(
					dependsOn.minus(test)
			)
		}

	}
}

allprojects {
	apply(plugin = "java")
	apply(plugin = "kotlin")
	apply(plugin = "architectury-plugin")

	base.archivesName.set(archivesBaseName)
	version = rootProject.version
	group = rootProject.group

	repositories {
		// Add repositories to retrieve artifacts from in here.
		// You should only use this when depending on other mods because
		// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
		// See https://docs.gradle.org/current/userguide/declaring_repositories.html
		// for more information about repositories.
	}

	dependencies {
		compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
	}

	tasks {
		withType(KotlinCompile::class) {
			with(kotlinOptions) {
				// https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/K2JVMCompilerArguments.kt
				freeCompilerArgs = listOf(
						"-Xjvm-default=all", "-Xlambdas=indy", "-Xtype-enhancement-improvements-strict-mode",
						"-Xmultifile-parts-inherit",
						"-Xbackend-threads=0", "-Xno-param-assertions", "-Xno-call-assertions",
						"-opt-in=kotlin.RequiresOptIn", "-Xextended-compiler-checks", "-Xassertions=jvm", "-progressive"
				)
				jvmTarget = javaVersion
				languageVersion = (kotlinSplitVersion[0] + '.' + (kotlinSplitVersion[1].toShort() + 1).toString())
				apiVersion = "${kotlinSplitVersion[0]}.${kotlinSplitVersion[1]}"
			}
		}
		withType(JavaCompile::class) {
			with(options) {
				encoding = "UTF-8"
				isFork = true
				release.set(javaVersion.toInt())
				sourceCompatibility = sourceJavaVersion
				targetCompatibility = javaVersion
			}
		}
	}

	java {
		withSourcesJar()
	}
}