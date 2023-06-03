plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

group = "fr.redstonneur1256"
version = "1.1"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.mc-skyplex.net/releases") }
}

dependencies {
    compileOnly(libs.anuken.arc)
    compileOnly(libs.anuken.mindustry)
    compileOnly(libs.modlib)
    implementation(libs.ipaddress)
}
