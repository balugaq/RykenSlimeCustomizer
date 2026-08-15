import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    java
    alias(libs.plugins.shadow)
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("maven-publish")
    id("signing")
    id("io.github.sgtsilvio.gradle.maven-central-publishing") version "0.5.0"
}

group = "io.github.balugaq"
val archiveName = "RykenSlimeCustomizer"
version = "3.0.6-test"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<Javadoc>().configureEach {
    // 出错（含 doclint 之外的警告）也不让 javadoc 任务失败，避免阻断构建/发布
    isFailOnError = false
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        charSet = "UTF-8"
        addStringOption("Xdoclint", "none")
    }
}

// 给所有 JavaExec 类任务（test / runServer / 以及其他 fork JVM 的任务）统一设置 UTF-8 编码，
// 避免因本地系统默认编码（如 GBK）导致乱码。
tasks.withType<JavaExec>().configureEach {
    systemProperty("file.encoding", "UTF-8")
    systemProperty("sun.stdout.encoding", "UTF-8")
    systemProperty("sun.stderr.encoding", "UTF-8")
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
    // compileOnly(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("**/*.yml") {
        expand(
            mapOf(
                "version" to project.version
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

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named<Javadoc>("javadoc"))
}

tasks.build {
    dependsOn(tasks.named("shadowJar"))
}

tasks.runServer {
    dependsOn(tasks.named("shadowJar"))

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
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5001",
        "-Dnet.kyori.adventure.text.warn_when_legacy_formatting_detected=false"
    )
    maxHeapSize = "4G"
    minecraftVersion("1.20.1")
}

publishing {
    repositories {
        maven {
            name = "Central"
            url = uri("https://central.sonatype.com/api/v1/publisher")
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.named("shadowJar"))
            // Maven Central 发布硬性要求：附带 sources / javadoc 构件
            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name = "RykenSlimeCustomizer"
                description = "A config-driven Slimefun addon engine: generate Slimefun items/machines from YAML files."
                url = "https://github.com/balugaq/RykenSlimeCustomizer"
                licenses {
                    license {
                        name = "GNU General Public License v3.0 or later"
                        url  = "https://www.gnu.org/licenses/gpl-3.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "balugaq"
                        name = "balugaq"
                        email = "balugaq@qq.com"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/balugaq/RykenSlimeCustomizer.git"
                    developerConnection = "scm:git:ssh://github.com/balugaq/RykenSlimeCustomizer.git"
                    url = "https://github.com/balugaq/RykenSlimeCustomizer"
                }
            }
        }
    }
}

// 签名配置
signing {
    // 从环境变量或 gradle.properties 读取敏感信息；
    // 仅在提供了签名密钥时才启用签名，避免本地 build/无密钥时配置失败
    val signingKey = providers.gradleProperty("signingKey").orNull
    val signingPassword = providers.gradleProperty("signingPassword").orNull
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    } else {
        // 未提供签名密钥（例如本地开发构建），跳过签名
    }
}