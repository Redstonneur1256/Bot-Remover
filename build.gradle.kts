plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

group = "fr.redstonneur1256"
version = System.getenv("GITHUB_VERSION") ?: "dev"

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository")
    maven("https://jitpack.io")
    maven("https://repo.mc-skyplex.net/releases")
}

dependencies {
    compileOnly(libs.anuken.arc)
    compileOnly(libs.anuken.mindustry)
    compileOnly(libs.modlib)
    compileOnly(libs.unifiedmetrics.api)
    implementation(libs.jsoup)
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
    outputs.upToDateWhen {
        false
    }
}

tasks.shadowJar {
    archiveFileName.set("Bot-Remover.jar")
}

if (file("private.gradle").exists()) {
    apply(from = "private.gradle")
}
