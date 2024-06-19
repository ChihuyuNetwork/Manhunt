plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    `maven-publish`
    `java-library`
    `kotlin-dsl`
    java
    idea
}

group = "love.chihuyu"
version = ""
val pluginVersion: String by project.ext

repositories {
    mavenCentral()
    maven("https://repo.codemc.org/repository/maven-public/")
//    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.purpurmc.org/snapshots")
}

/*
1.7.10~1.8.8: "org.github.paperspigot:paperspigot-api:$pluginVersion-R0.1-SNAPSHOT"
1.9.4~1.16.5: "com.destroystokyo.paper:paper-api:$pluginVersion-R0.1-SNAPSHOT"
1.17~1.19.4: "io.papermc.paper:paper-api:$pluginVersion-R0.1-SNAPSHOT"
 */

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.21-R0.1-SNAPSHOT")
    compileOnly("dev.jorel:commandapi-bukkit-core:9.5.1")
    compileOnly("dev.jorel:commandapi-bukkit-kotlin:9.5.1")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

ktlint {
    ignoreFailures.set(true)
    disabledRules.add("no-wildcard-imports")
}

tasks {
    test {
        useJUnitPlatform()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(sourceSets.main.get().resources.srcDirs) {
            filter(
                org.apache.tools.ant.filters.ReplaceTokens::class,
                mapOf(
                    "tokens" to mapOf(
                        "version" to project.version.toString(),
                        "name" to project.name,
                        "mainPackage" to "love.chihuyu.${project.name.lowercase()}.${project.name}Plugin"
                    )
                )
            )
            filteringCharset = "UTF-8"
        }
    }

    shadowJar {
        exclude("org/slf4j/**")
        relocate("kotlin", "love.chihuyu.${project.name.lowercase()}.lib.kotlin")
        archiveClassifier.set("")
    }

    runServer {
        minecraftVersion(pluginVersion)
    }
}

kotlin {
    jvmToolchain(21)
}
