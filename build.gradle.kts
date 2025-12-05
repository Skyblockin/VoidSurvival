plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("io.github.goooler.shadow") version "8.1.8"
    id("xyz.jpenilla.run-paper") version "3.0.0" // Adds runServer and runMojangMappedServer tasks for testing
}

group = "com.skyblockin.voidsurvival"
version = "1.0"
description = "Simple plugin for generating floating islands in ungenerated chunks"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    maven("https://maven.enginehub.org/repo/")
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.14")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.17")
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release = 21
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    assemble {
        dependsOn(reobfJar)
    }
}