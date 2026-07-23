import java.util.zip.ZipFile

plugins {
    `java-library`
    `maven-publish`
}

group = "com.hyrhythm"
extra["versionTagPrefix"] = "HyRhythm"
apply(from = "gradle/git-version.gradle.kts")
version = extra["gitVersion"] as String

val hytaleServerVersion = "2026.02.19-1a311a592"
val hytaleServerCoordinates = "com.hypixel.hytale:Server:$hytaleServerVersion"
val pluginManifestVersion = version.toString().let { buildVersion ->
    if (buildVersion.endsWith("-SNAPSHOT")) {
        "${buildVersion.substringBefore('-')}-SNAPSHOT"
    } else {
        buildVersion
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            name = "CodeMCHytale"
            url = uri("https://repo.codemc.io/repository/hytale/")
        }
    }

    dependencyLocking {
        lockAllConfigurations()
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
    withJavadocJar()
}

val hytaleServer = configurations.create("hytaleServer") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    compileOnly(hytaleServerCoordinates)
    testImplementation(hytaleServerCoordinates)
    hytaleServer(hytaleServerCoordinates)

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
}

sourceSets {
    test {
        java.srcDir("src/serviceLoaderTest/java")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs(
        "-Djava.util.logging.manager=com.hypixel.hytale.logger.backend.HytaleLogManager",
        "-Dnet.bytebuddy.experimental=true",
        "-XX:+EnableDynamicAgentLoading",
    )
}

tasks.withType<Jar>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.processResources {
    inputs.property("pluginManifestVersion", pluginManifestVersion)
    filesMatching("manifest.json") {
        expand(
            mapOf(
                "project" to mapOf(
                    "groupId" to project.group.toString(),
                    "name" to rootProject.name,
                    "version" to pluginManifestVersion,
                ),
                "plugin" to mapOf(
                    "main" to mapOf("class" to "com.hyrhythm.HyRhythmPlugin"),
                ),
            ),
        )
    }
}

tasks.jar {
    archiveFileName = "HyRhythm.jar"
}

val verifyJarContents = tasks.register("verifyJarContents") {
    dependsOn(tasks.jar)
    val archive = tasks.jar.flatMap { it.archiveFile }
    inputs.file(archive)
    doLast {
        ZipFile(archive.get().asFile).use { jar ->
            val embedded = jar.entries().asSequence()
                .map { it.name }
                .firstOrNull { it.startsWith("com/hypixel/hytale/") }
            check(embedded == null) { "Hytale server class embedded in plugin JAR: $embedded" }
        }
    }
}

tasks.check {
    dependsOn(verifyJarContents)
}

val prepareHytaleServerReference = tasks.register<Sync>("prepareHytaleServerReference") {
    from(hytaleServer)
    into(layout.buildDirectory.dir("hytale-server"))
    rename { "Server-$hytaleServerVersion.jar" }
}

val stageDistribution = tasks.register<Sync>("stageDistribution") {
    dependsOn(tasks.jar)
    into(layout.projectDirectory.dir("distribution"))
    from(tasks.jar.flatMap { it.archiveFile }) {
        into("mods")
    }
}

tasks.assemble {
    dependsOn(prepareHytaleServerReference, stageDistribution)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "hyrhythm"
        }
    }
    repositories {
        val token = providers.environmentVariable("GITHUB_TOKEN")
        if (token.isPresent) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/TavallStudios/HyRhythm")
                credentials {
                    username = providers.environmentVariable("GITHUB_ACTOR").orNull
                    password = token.get()
                }
            }
        }
    }
}

project(":hytale-server-patch") {
    apply(plugin = "java-library")

    group = rootProject.group
    version = rootProject.version

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
    }

    val patchServer = configurations.create("patchServer") {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
    }

    dependencies {
        "compileOnly"(hytaleServerCoordinates)
        add(patchServer.name, hytaleServerCoordinates)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release = 25
        options.encoding = "UTF-8"
    }

    tasks.withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    val patchSourceSets = extensions.getByType<SourceSetContainer>()
    val patchedServerJar = tasks.register("patchedServerJar") {
        dependsOn(tasks.named("classes"))
        val outputJar = layout.buildDirectory.file(
            "distributions/Server-$hytaleServerVersion-patched.jar",
        )
        inputs.files(patchServer)
        inputs.files(patchSourceSets.named("main").map { it.output })
        outputs.file(outputJar)

        doLast {
            val sourceJar = patchServer.singleFile
            val targetJar = outputJar.get().asFile
            targetJar.parentFile.mkdirs()
            sourceJar.copyTo(targetJar, overwrite = true)

            patchSourceSets.named("main").get().output.files
                .filter(File::exists)
                .forEach { output ->
                    providers.exec {
                        commandLine(
                            javaToolchains.launcherFor {
                                languageVersion = JavaLanguageVersion.of(25)
                            }.get().metadata.installationPath.file("bin/jar").asFile,
                            "uf",
                            targetJar,
                            "-C",
                            output,
                            ".",
                        )
                    }.result.get().assertNormalExitValue()
                }
        }
    }

    tasks.named("assemble") {
        dependsOn(patchedServerJar)
    }
}
