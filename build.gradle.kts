import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.ProviderDelegate
import org.jetbrains.kotlin.util.removeSuffixIfPresent

// Some Utility Definitions
// ========================

buildscript {
    repositories {
        // This is here to fix a missing dependency issue with intelliJ plugin introduced in 0.25 und still present 
        // in 0.26. It should hopefully be fixed in 0.27.
        maven {
            url = uri("https://jetbrains.bintray.com/intellij-plugin-service")
        }
    }
}

// these two allow to conveniently use the version reckon ... reckons without the commit hash/timestamp it appends
// to insignificant versions. Please leave this here even if it's not currently used.
//
// # Detailed reasoning:
// Why this is useful? One of the best cases of this being useful I have found so far is that by default Gradle's jar
// task appends the version to the end of the jars name (as is common in the Java world). With the unmodified reckon
// version this means that unless you are building from a clean git tag, every build will produce a new and uniquely
// named jar file, even if the contents are the same, because the version reckon returns changes by the millisecond.
// In some cases it may be desirable to have these unique build, perhaps to directly compare different implementations
// of something before deciding which to commit. But in other cases it only needlessly increases the size of the build
// directly.
// Reckon, by design, as of writing this does not provide a way to disable this behaviour so instead I came up with
// this.
//
// use hashlessVersion in places where you want reckons version without hash or timestamp. You may use
// hashlessVersionProvider directly when whatever function the version is passed to happens to also accept providers.
// The reason I'm using a provider in the first place is that reckon replaces the version properties with a lambda
// that is lazily evaluated when the version is accessed. As such it is not guaranteed to be safe to read at all times,
// especially during configuration phase.
//
// Note that as of writing this will only remove the hash of dev version. I did this to avoid unexpectedly different
// versions whenever possible and most of the cases in which this will be useful (I.e. during fast iteration) are
// expected to be dev versions.
val hashlessVersionProvider = provider {
    project.version.toString()
        .replaceAfterLast("dev", "")
        .removeSuffixIfPresent("+")
}
val hashlessVersion by ProviderDelegate<String> { hashlessVersionProvider.get() }

// Try to keep all dependency related version stuff here for easy of maintainability
val versionJUnit by rootProject.extra("5.7.0")
val versionStrikt by rootProject.extra("0.27.0")

// Minimum version of Idea to build against. Also used by the intelliJ plugin to resolve plugin dependencies.
val ideaVersion by rootProject.extra("2020.2")

plugins {
    // Required for buildings
    kotlin("jvm").version("1.4.10")
    id("org.jetbrains.intellij").version("0.4.26")

    // Extra stuff
    // Reckon automatically determines a project version based on git status.
    id("org.ajoberstar.reckon").version("0.12.0")

    // detekt is used for code quality. The Detekt Idea plugin can be used as well.
    // There are some things Detekt can ... detect, that IntelliJs inspections won't.
    // See Detekt.yml in config/detekt
    // Note that I don't require everything Detekt reports to be fixed. For example, comment related rules have a weight
    // of 0 because they don't affect the resulting jar's quality. However, using the config file in this project,
    // Detekt will fail builds if too many of certain issues were found.
    id("io.gitlab.arturbosch.detekt").version("1.12.0-RC1")

    // I use the idea plugin to have IntelliJ download sources and docs for any dependencies automatically. Not
    // required at all. I believe the IntelliJ plugin implicitly applies the idea plugin but I'm a fan of being
    // explicit about these things.
    idea
}

repositories {
    mavenCentral()
    jcenter()
}

project.group = "lafreakshow.plugins"

tasks.runIde {
    // It took me a while to figure out to use this reliably. The problem was that a simple build isn't enough. Run
    // the prepareSandbox task, which will build the jar and replace the plugin in the sandbox, at which point
    // IntelliJ can pick up on the change and reload. Note that some changes lead to failed unloads at which point
    // you need to restart the development instance. I've had such problems mostly with changing the Icons and
    // occasionally with minor changes within a function.
    autoReloadPlugins = true
}

intellij {
    // the lowest version that has to be supported is 2020.2 (because it's the one I use as of writing this)
    // but support for earlier versions may be added if there is demand.
    version = ideaVersion

    setPlugins("java")
    updateSinceUntilBuild = true
    configureDefaultDependencies = true
}

dependencies {
    // IntelliJ 2020.2 comes with Kotlin 1.3.73 so to make sure that we can use 1.4 features we depend on it explicitly.
    implementation(kotlin("reflect", "1.4+"))
    implementation(kotlin("stdlib", "1.4+"))

    testImplementation("io.strikt:strikt-core:$versionStrikt")
    testImplementation("org.junit.jupiter:junit-jupiter:$versionJUnit")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$versionJUnit")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$versionJUnit")
}

tasks.jar {
    // Reckon appends a commit hash or timestamp to every version between two releases. If this would end up in the jars
    // filename IntelliJ would not be able to detect changes for the purpose of hot reloading the plugin.
    archiveVersion.set(hashlessVersionProvider)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
        resourceDirs = mutableSetOf(
            file("src/main/resources"),
            // Workaround for a bug in IntelliJ where it will include lots of empty packages from dependencies in
            // the package view if any resource directory containing a META-INF folder was added.
            // Addendum: Apparently the Icons directory also triggers this. At least there it's only a dozen packages
            // instead of a couple hundred.
            file("src/main/resources/META-INF/")
        )
    }
}

// Configuration affecting details of the build process
// ====================================================

// most of Detekt's config is taken from config/detekt/detekt.yml but there are some defaults we need to set here
detekt {
    // This tells Detekt where to look for sources
    // Note that because there are no Java sources as of writing this, the first line is not needed. But I like to keep
    // it around because 1) in case someone does add java sources and 2) because when working with Java Modules, even if
    // written in kotlin, there will often be a module-info.java in the Java source set.
    input = objects.fileCollection().from(
        DetektExtension.DEFAULT_SRC_DIR_JAVA, "src/test/java",
        DetektExtension.DEFAULT_SRC_DIR_KOTLIN, "src/test/kotlin",
        "src/languageJavaLike/kotlin"
    )

    // This will make detekt start with the default rules enabled and then apply any custom configuration from config
    // files on top. This means that theoretically we'd only need to include things in detekt.yml that are different
    // than in the default. With tools like detekt I like to keep a full config file around that has all possible
    // options if not applied then at least in a comment. Makes it easier to quickly change settings and avoids
    // confusion because of implied configuration.
    buildUponDefaultConfig = true
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            // some IntelliJ platform interfaces use default methods, to be able to use these in Kotlin we must
            // enable it explicitly.
            "-Xjvm-default=enable"
        )

        languageVersion = "1.4"
        apiVersion = "1.4"
    }
}


// reckon needs to have some info passed via properties to create tags.
reckon {
    scopeFromProp()
// "dev"   : builds produced during active development and iteration
// "rc"    : builds that are assumed to be release ready but are still being polished and tested
// "final  : builds that are ready to be released to the public.
// There is no beta tag here, any version below 1.0.0 is considered beta, build released to the public as a
// preview should be rc.
//
// Note that reckon defaults to the alphanumerical first, dev in this case. This is consistent with how gradle
// sorts versions.
    stageFromProp("dev", "rc", "final")
}

// Make sure the project is in reasonable shape before allowing a tag to be created. All in all this requires the repo
// to be clean, a build to succeed, a non-build-failing run of detekt and tests to pass.
tasks.reckonTagCreate {
    dependsOn(":check")
}

tasks.test {
    useJUnitPlatform()
}

// When running under Java9+, the debugger needs some permissions to be able and attach to the development instance
// technically only needed when actually debugging but I added it to any task that I noticed output a warning for this.
listOf("buildSearchableOptions", "runIde").forEach {
    tasks.named(it, JavaForkOptions::class).configure {
        jvmArgs("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
    }
}
