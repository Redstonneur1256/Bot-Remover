plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

group = "fr.redstonneur1256"
version = "1.3.2"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.mc-skyplex.net/releases") }
}

dependencies {
    compileOnly(libs.anuken.arc)
    compileOnly(libs.anuken.mindustry)
    compileOnly(libs.modlib)
    compileOnly(libs.unifiedmetrics.api)
    implementation(libs.ipaddress)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.processResources {
    filesMatching("plugin.json") {
        expand("version" to project.version)
    }
}

tasks.shadowJar {
    archiveFileName.set("Bot-Remover.jar")
}
