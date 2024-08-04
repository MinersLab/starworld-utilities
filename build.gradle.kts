import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("fabric-loom") version "1.7-SNAPSHOT"
	id("maven-publish")
	id("org.jetbrains.kotlin.jvm") version "2.0.0"
}

version = property("mod_version").toString()
group = property("maven_group").toString()

base {
	archivesName = property("archives_base_name").toString()
}

repositories {}

loom {
	splitEnvironmentSourceSets()

	mods {
		create("starworldcorelib") {
			sourceSet(sourceSets.getByName("main"))
			sourceSet(sourceSets.getByName("client"))
		}
	}

}

fabricApi {
	configureDataGeneration()
}

dependencies {
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
}


tasks.withType<ProcessResources> {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to project.version)
	}
}


tasks.withType<JavaCompile> {
	options.release = 17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_17
	}
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Jar> {
	from("LICENSE") {
		rename { "${it}_${project.base.archivesName.get()}"}
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = property("archives_base_name") as String
			from(components.getByName("java"))
		}
	}
	repositories {}
}