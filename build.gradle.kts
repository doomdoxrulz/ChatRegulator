import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation

plugins {
    java
    id("net.kyori.blossom") version "1.3.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
}

repositories {
    mavenLocal()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.fvdh.dev/releases")
    maven("https://jitpack.io")
    maven("https://repo.alessiodp.com/releases/")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnly("org.spongepowered:configurate-hocon:4.1.2")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.0.6")
    shadow("org.jetbrains:annotations:23.0.0")
    shadow("net.byteflux:libby-velocity:1.1.5")

    compileOnly("com.github.4drian3d:MiniPlaceholders:1.1.1")

    compileOnly("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")

    testImplementation("org.slf4j:slf4j-api:1.7.32")
    testImplementation("org.spongepowered:configurate-hocon:4.1.2")
    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:4.1.0")
    testImplementation("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")
    testImplementation("com.github.ben-manes.caffeine:caffeine:3.0.6")
}

group = "me.dreamerzero.chatregulator"
version = "3.0.1-SNAPSHOT"
description = "A global chat regulator for you Velocity network"
val url: String = "https://forums.velocitypowered.com/t/chatregulator-a-global-chat-regulator-for-velocity/962"
val id: String = "chatregulator"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            artifactId = id
            version = version
            from(components["java"])
        }
    }
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).links(
        "https://jd.adventure.kyori.net/api/4.10.1/",
        "https://jd.adventure.kyori.net/text-minimessage/4.10.1/",
        "https://jd.velocitypowered.com/3.0.0/"
    )
}

blossom{
	val constants: String = "src/main/java/me/dreamerzero/chatregulator/utils/Constants.java"
	replaceToken("{name}", rootProject.name, constants)
    replaceToken("{id}", id, constants)
	replaceToken("{version}", version, constants)
	replaceToken("{description}", description, constants)
    replaceToken("{url}", url, constants)
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        dependsOn(getByName("relocateShadowJar") as ConfigureShadowRelocation)
        minimize()
        archiveFileName.set("ChatRegulator.jar")
        configurations = listOf(project.configurations.shadow.get())
    }

    create<ConfigureShadowRelocation>("relocateShadowJar") {
        target = shadowJar.get()
        prefix = "me.dreamerzero.chatregulator.libs"
    }
    test {
        useJUnitPlatform()
        testLogging {
		    events("passed", "skipped", "failed")
	    }
    }

    compileJava {
        options.release.set(16)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
