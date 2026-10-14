plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.24"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.0" // Adds runServer and runMojangMappedServer tasks for testing
}

group = "com.skyblockin.voidsurvival"
version = "1.0"
description = "Simple plugin for generating floating islands in ungenerated chunks"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
    maven("https://maven.enginehub.org/repo/")
    maven {
        name = "citizens-repo"
        url = uri("https://maven.citizensnpcs.co/repo")
    }
}

dependencies {

    paperweight.paperDevBundle("26.2.build.+")

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.16")

    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.5")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.19")

    compileOnly("dev.aurelium:auraskills-api-bukkit:2.2.4")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.22.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.22.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.22")
    implementation("com.fasterxml.jackson.core:jackson-core:2.22.1")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.22.1")

    compileOnly("net.citizensnpcs:citizens-main:2.0.44-SNAPSHOT") {
        exclude("*", "*")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(25)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    assemble {
        dependsOn(shadowJar)
    }

    shadowJar {
        // Enforce parsing across the active execution classpath
        configurations = listOf(project.configurations.getByName("runtimeClasspath"))

        // Relocate dependencies cleanly out of vanilla namespace environments
        relocate("com.fasterxml.jackson", "com.skyblockin.voidsurvival.libs.jackson")

        // Prevents bloating the jar with unused parts of the Jackson library
        minimize()

        // Classify the output cleanly so Gradle knows it's the main production file
        archiveClassifier.set("")
    }
}