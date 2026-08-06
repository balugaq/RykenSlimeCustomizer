import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    java
    // alias(libs.plugins.spotless)
    alias(libs.plugins.shadow)
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "org.lins.mmmjjkx"
val archiveName = "RykenSlimeCustomizer"
version = "29.1-Modified"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(21)
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.alessiodp.com/releases/")
    maven("https://jitpack.io")
    maven("https://repo.minebench.de/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    implementation(libs.libby.bukkit)
    implementation(libs.uni.item.all) {
        exclude(group = "io.github.projectunified", module = "uni-item-slimefun")
    }

    compileOnly(libs.graalvm.js)
    compileOnly(libs.graalvm.js.language)
    compileOnly(libs.graalvm.js.scriptengine)
    compileOnly(libs.graalvm.shadowed.icu4j)
    compileOnly(libs.graalvm.truffle.api)
    compileOnly(libs.graalvm.truffle.compiler)
    compileOnly(libs.graalvm.truffle.enterprise)
    compileOnly(libs.graalvm.truffle.runtime)
    compileOnly(libs.graalvm.polyglot)
    compileOnly(libs.graalvm.sdk.collections)
    compileOnly(libs.graalvm.sdk.nativeimage)
    compileOnly(libs.graalvm.sdk.word)
    compileOnly(libs.graalvm.sdk.nativebridge)
    compileOnly(libs.graalvm.sdk.jniutils)
    compileOnly(libs.graalvm.regex)
    compileOnly(libs.guizhan.lib.plugin)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.byte.buddy)
    compileOnly(libs.paper.api)
    compileOnly(libs.slimefun4)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.item.nbt.api.plugin)
    compileOnly(libs.justenoughguide)

    // System-scoped local JARs
    compileOnly(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
}

tasks.jar {
    enabled = false
}

// spotless {
//     java {
//         googleJavaFormat()
//         removeUnusedImports()
//         formatAnnotations()
//         importOrder()
//         licenseHeader(file("header.txt").readText())
//     }
// }

tasks.named<ProcessResources>("processResources") {
    filesMatching("**/*.yml") {
        expand(
            mapOf(
                "version" to project.version,
                "project" to mapOf("version" to project.version)
            )
        )
    }
}

tasks.named<ShadowJar>("shadowJar") {

    archiveBaseName.set(archiveName) // Don't change it, it's used to fix build station identifier issue
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")
    relocate("io.github.projectunified.uniitem", "org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.uniitem")
    relocate("net.byteflux.libby", "org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.libby")
}

tasks.build {
    dependsOn(tasks.named("shadowJar"))
}

tasks.runServer {
    dependsOn(tasks.named("shadowJar"))

    systemProperty("file.encoding", "UTF-8")
    systemProperty("sun.stdout.encoding", "UTF-8")
    systemProperty("sun.stderr.encoding", "UTF-8")

    doFirst {
        val run = projectDir.resolve("run")
        run.mkdirs()
        run.resolve("eula.txt").writeText("eula=true")

        val pl = run.resolve("plugins")
        pl.mkdirs()
        copy {
            from(projectDir.resolve("build/libs")) {
                include("${archiveName}-${version}.jar")
            }
            into(pl)
        }
    }

    jvmArgs(
        "-Dfile.encoding=UTF-8",
        "-Dsun.jnu.encoding=UTF-8",
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5001"
    )
    maxHeapSize = "4G"
    minecraftVersion("1.20.1")
}