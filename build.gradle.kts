import io.gitlab.arturbosch.detekt.extensions.DetektExtension

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.ProviderDelegate
import org.jetbrains.kotlin.util.removeSuffixIfPresent

// Some Utility Definitions
// ========================

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
val hashlessVersionProvider: Provider<String> = provider {
    project.version.toString()
        .replaceAfterLast("dev", "")
        .removeSuffixIfPresent("+")
}
val hashlessVersion: String by ProviderDelegate { hashlessVersionProvider.get() }

// Try to keep all dependency related version stuff here for easy of maintainability
val versionJUnit: String by rootProject.extra("5.7.0")
val versionStrikt: String by rootProject.extra("0.28.0")
val kotlinLibVersion: String by rootProject.extra("1.4+")

// Minimum version of Idea to build against. Also used by the intelliJ plugin to resolve plugin dependencies.
val ideaVersion: String by rootProject.extra("2020.2")

plugins {
    // Required for buildings
    kotlin("jvm").version("1.4.10")
    id("org.jetbrains.intellij").version("0.6.1")

    // Extra stuff
    // Reckon automatically determines a project version based on git status.
    id("org.ajoberstar.reckon").version("0.13.0")

    // detekt is used for code quality. The Detekt Idea plugin can be used as well.
    // There are some things Detekt can ... detect, that IntelliJs inspections won't.
    // See Detekt.yml in config/detekt
    // Note that I don't require everything Detekt reports to be fixed. For example, comment related rules have a weight
    // of 0 because they don't affect the resulting jar's quality. However, using the config file in this project,
    // Detekt will fail builds if too many of certain issues were found.
    id("io.gitlab.arturbosch.detekt").version("1.14.2")

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

    setPlugins("java", "Kotlin")
    updateSinceUntilBuild = true
    configureDefaultDependencies = true
}

val priority: Configuration by configurations.creating

dependencies {
    // IntelliJ 2020.2 comes with Kotlin 1.3.73 so to make sure that we can use 1.4 features we depend on it explicitly.
    //
    // Note that with a dependency on the kotlin plugin in plugin.xml I'm having issues getting the right version of
    // the kotlin std lib and reflect to load both during compile and at runtime. The problem here is that tmy
    // explicitly declared dependencies for some reason end up further down the classpath than the jard that come
    // with IntelliJ, which causes the Classloader to load an incompatible version of certain classes.
    //
    // After spending the entire day on a solution that was almost longer than the entire entire build script and
    // barely functioning on good days, It turns out that a simple custom configuration can do it way more reliable.
    // So this is what the priority configuration is all about. Further down it is prepended to the source sets
    // classpath, which is the part that actually solves the issue.
    priority(kotlin("stdlib", kotlinLibVersion))
    priority(kotlin("reflect", kotlinLibVersion))

    // For some reason without this kotlin reflect, and only kotlin reflect, doesn't end up in the distribution image.
    // I'm not quite sure why this happens. By which I mean I absolutely no idea (pun not intended).
    configurations.runtimeClasspath.configure { extendsFrom(priority) }

    testImplementation("io.strikt:strikt-core:$versionStrikt")
    testImplementation("org.junit.jupiter:junit-jupiter:$versionJUnit")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$versionJUnit")
}

sourceSets.main.configure {
    compileClasspath = priority + compileClasspath
    runtimeClasspath = priority + runtimeClasspath
}
sourceSets.test.configure {
    compileClasspath = priority + compileClasspath
    runtimeClasspath = priority + runtimeClasspath
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
    }
}

logger.lifecycle(
    buildString {
        val install = javaInstalls.installationForCurrentVirtualMachine.get()
        append("Running ")
        append(install.implementationName)
        append(":")
        append(install.javaVersion)
        append(" from ")
        append(install.installationDirectory)
    }
)

// Configuration affecting details of the build process
// ====================================================

// most of Detekt's config is taken from config/detekt/detekt.yml but there are some defaults we need to set here
detekt {
    // This tells Detekt where to look for sources
    // Note that because there are no Java sources as of writing this, the first line is not needed. But I like to keep
    // it around because 1) in case someone does add java sources and 2) because when working with Java Modules, even if
    // written in kotlin, there will often be a module-info.java in the Java source set.
    input = objects.fileCollection().from(
        DetektExtension.DEFAULT_SRC_DIR_JAVA,
        "src/test/java",
        DetektExtension.DEFAULT_SRC_DIR_KOTLIN,
        "src/test/kotlin"
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

tasks.withType<KotlinCompile> {
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
    dependsOn(":runPluginVerifier")
}

tasks.test {
    useJUnitPlatform()

    // In some cases it is desired to change the logging setting for the actual test running JVM, this is
    // the most convenient way.
    jvmArgs("-Djava.util.logging.config.file=src/test/resources/logging.properties")
}

// When running under Java9+, the debugger needs some permissions to be able and attach to the development instance
// technically only needed when actually debugging but I added it to any task that I noticed output a warning for this.
listOf("buildSearchableOptions", "runIde").forEach {
    tasks.named(it, JavaForkOptions::class).configure {
        jvmArgs("--add-exports", "java.base/jdk.internal.vm=ALL-UNNAMED")
    }
}
