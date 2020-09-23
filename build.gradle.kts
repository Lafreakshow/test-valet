import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.ProviderDelegate
import org.jetbrains.kotlin.util.removeSuffixIfPresent

// Some Utility Definitions
// ========================

val hashlessVersionProvider = provider {
    project.version.toString()
        .replaceAfterLast("dev", "")
        .removeSuffixIfPresent("+")
}
val hashlessVersion by ProviderDelegate<String> { hashlessVersionProvider.get() }

val versionJUnit by rootProject.extra("5.7.0")
val versionStrikt by rootProject.extra("0.27.0")

plugins {
    // Required for buildings
    kotlin("jvm").version("1.4.10")
    id("org.jetbrains.intellij").version("0.4.22")

    // Extra stuff
    // Reckon automatically determines a project version based on git status.
    id("org.ajoberstar.reckon").version("0.12.0")

    // detekt is used for code quality. The Detekt Idea plugin can be used as well.
    // There are some things Detekt can ... detect, that IntelliJs inspections won't.
    // See Detekt.yml in config/detekt
    // Note that I don't require everything Detekt reports to be fixed. For example, comment related rules have a wright
    // of 0 because they don't affect the resulting jar's quality. However, using the config file in this project,
    // Detekt will fail builds if too many of certain issues were found.
    id("io.gitlab.arturbosch.detekt").version("1.12.0-RC1")

    // I use the idea plugin to have IntelliJ download sources and docs for any dependencies automatically.
    // Not required at all.
    idea
}

// Required configurations for build script
// ========================================

repositories {
    mavenCentral()
    jcenter()
}

project.group = "lafreakshow.plugins"

dependencies {
    implementation(kotlin("reflect", "1.4+"))
    implementation(kotlin("stdlib", "1.4+"))

    testImplementation("io.strikt:strikt-core:$versionStrikt")
    testImplementation("org.junit.jupiter:junit-jupiter:$versionJUnit")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$versionJUnit")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$versionJUnit")
}

tasks.runIde {
    autoReloadPlugins = true
}

// Configuration of the plugin produced by this build
// ==================================================

intellij {
    // the lowest version that has to be supported is 2020.2 (because it's the one I use as of writing this)
    // but support for earlier versions may be added if there is demand.
    version = "2020.2"

    setPlugins("java")
    updateSinceUntilBuild = false
}

tasks.jar {
    // Reckon appends a commit has or timestamp to every version between two releases. If this would end up in the jars
    // filename IntelliJ would not be able to detect changes for the purpose of hot reloading the plugin.
    archiveVersion.set(hashlessVersionProvider)
}

// Configuration affecting details of the build process
// ====================================================

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

listOf("compileKotlin", "compileTestKotlin").forEach {
    tasks.getByName<KotlinCompile>(it) {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=enable")

        kotlinOptions.languageVersion = "1.4"
        kotlinOptions.apiVersion = "1.4"
    }
}

// as stated in plugin block
idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
        resourceDirs = mutableSetOf(
            file("src/main/resources"),
            // Workaround for a bug in IntelliJ where it would include lots of empty packages
            // from dependencies in the package view if any resource directory containing a
            // META-INF folder was added.
            file("src/main/resources/META-INF/")
        )
    }
}

// most of Detekt's config is taken from config/detekt/detekt.yml but there are some defaults we need to set here
detekt {
    // This tells Detekt where to look for sources
    // Note that because there are no Java sources as of writing this, the first line is not needed. But I like to keep
    // it around because 1) in case someone does add java sources and 2) because when working with Java Modules, even if
    // written in kotlin, there will be a module-info.java in the Java source set.
    input = objects.fileCollection().from(
        DetektExtension.DEFAULT_SRC_DIR_JAVA, "src/test/java",
        DetektExtension.DEFAULT_SRC_DIR_KOTLIN, "src/test/kotlin"
    )

    // This will make detekt start with the default rules enabled and then apply any custom configuration from config
    // files on top. This means that theoretically we'd only need to include things in detekt.yml that are different
    // than in the default. With tools like detekt I like to keep a full config file around that has all possible
    // options if not applied then at least in a comment. Makes it easier to quickly change settings and avoids
    // confusion because of implied configuration.
    buildUponDefaultConfig = true
}

// reckon needs to have some info passed via properties to create tags.
reckon {
    scopeFromProp()
    // "dev"   : builds produced during active Development and iteration
    // "rc"    : builds that are assumed to be release ready but are still being polished and tested
    // "final : builds that are ready to be released to the public.
    // There is no beta tag here, any version below 1.0.0 is considered beta.
    stageFromProp("dev", "rc", "final")
}

// Make sure the project is in reasonable shape before allowing a tag to be created. All in all this requires the repo
// to be clean, a build to succeed, a non-build-failing run of detekt and tests to pass.
tasks.reckonTagCreate {
    dependsOn(":common:check")
}

tasks.test {
    useJUnitPlatform()
}

// When running under Java9+, the debugger needs some permissions to be able and attach to the development instance
// technically only needed when actually debugging but I added it to any task that output a warning for this.
listOf("buildSearchableOptions", "runIde").forEach {
    tasks.named(it, JavaForkOptions::class).configure {
        jvmArgs("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
    }
}
