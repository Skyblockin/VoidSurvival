plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.0" // Adds runServer and runMojangMappedServer tasks for testing
}

group = "com.skyblockin.voidsurvival"
version = "1.0"
description = "Simple plugin for generating floating islands in ungenerated chunks"

repositories {
    maven("https://maven.enginehub.org/repo/")
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.14")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.17")

    compileOnly("dev.aurelium:auraskills-api-bukkit:2.2.4")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    assemble {
        dependsOn(reobfJar)
    }
}