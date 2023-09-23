import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("com.github.johnrengelman.shadow") version ("7.1.2")
    kotlin("jvm") version "1.8.21"
}

group = "com.mcyzj"
version = "1.0.0"

repositories {
    mavenCentral()
    maven ("https://repo.papermc.io/repository/maven-public/")
    maven ("https://oss.sonatype.org/content/groups/public/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://maven.xbaimiao.com/repository/releases/")
    maven("https://repo1.maven.org/maven2/")
    maven {
        url = uri("https://maven.xbaimiao.com/repository/maven-private/")
        credentials {
            username = project.findProperty("BaiUser").toString()
            password = project.findProperty("BaiPassword").toString()
        }
    }
    mavenLocal()
}

dependencies {
    implementation ("com.j256.ormlite:ormlite-core:6.1")
    implementation ("com.j256.ormlite:ormlite-jdbc:6.1")
    implementation ("com.zaxxer:HikariCP:4.0.3")
    implementation("com.xbaimiao:EasyLib:2.1.6")
    compileOnly(dependencyNotation = "org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly(fileTree("libs"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}