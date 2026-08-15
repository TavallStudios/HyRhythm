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
val tavallToolsVersion = "1.0.0"
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
        val githubToken = providers.environmentVariable("GITHUB_TOKEN").orNull
        if (!githubToken.isNullOrBlank()) {
            listOf(
                "tavall-di",
                "tavall-cache",
                "tavall-concurrency",
                "tavall-database",
                "tavall-eventbus",
                "tavall-logging",
                "tavall-reflection",
                "tavall-registry",
                "tavall-scheduler",
            ).forEach { repository ->
                maven("https://maven.pkg.github.com/TavallStudios/$repository") {
                    name = "github${repository.replace("-", "")}"
                    credentials {
                        username = providers.environmentVariable("GITHUB_ACTOR").orElse("github").get()
                        password = githubToken
                    }
                }
            }
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
    implementation("org.tavall:tavall-di:$tavallToolsVersion")
    implementation("org.tavall:tavall-registry:$tavallToolsVersion")
    implementation("org.tavall:tavall-logging:$tavallToolsVersion")
    implementation("org.tavall:tavall-concurrency:$tavallToolsVersion")
    implementation("org.tavall:tavall-scheduler:$tavallToolsVersion")
    implementation("org.tavall:tavall-eventbus:$tavallToolsVersion")
    implementation("org.tavall:tavall-reflection:$tavallToolsVersion")

    compileOnly(hytaleServerCoordinates)
    testImplementation(hytaleServerCoordinates)
    hytaleServer(hytaleServerCoordinates)

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
}

sourceSets {
    test {
        // Transitional first-party bootstrap/dependency-loader isolation coverage.
        // Remove with the custom DependencyLoader/BootstrapRegistry stack as Tavall DI/Registry take ownership.
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
        // This is still Tavall-owned Java, even though it patches an external Hytale host artifact.
        "implementation"("org.tavall:tavall-di:$tavallToolsVersion")
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
