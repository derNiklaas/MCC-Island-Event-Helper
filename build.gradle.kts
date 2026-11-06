import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.10"
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
    id("maven-publish")
}

val target = project.property("target") as String
val targetProperties = Properties().apply {
    val file = file("versions/$target.properties")
    require(file.isFile) { "No versions/$target.properties. Known targets: ${file("versions").list()?.sorted()}" }
    file.reader(Charsets.UTF_8).use(::load)
}

fun targetProperty(name: String): String =
    targetProperties.getProperty(name) ?: error("versions/$target.properties is missing $name")

val minecraftVersion = targetProperty("minecraft_version")
val yaclVersion = targetProperty("yacl_version")
val sheeplibVersion = targetProperty("sheeplib_version")
val noxesiumVersion = targetProperty("noxesium_version")

version = "${project.property("mod_version")}+$target"
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 25
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}



repositories {
    maven("https://maven.isxander.dev/releases") {
        name = "Xander Maven"
        content { includeGroup("dev.isxander") }
    }
    maven("https://maven.quiltmc.org/repository/release") {
        name = "Quilt"
        // YACL's JSON5 parser
        content { includeGroup("org.quiltmc.parsers") }
    }
    maven("https://maven.terraformersmc.com/releases") {
        name = "TerraformersMC"
        content { includeGroup("com.terraformersmc") }
    }
    maven("https://maven.noxcrew.com/public") {
        name = "Noxcrew"
        content { includeGroupAndSubgroups("com.noxcrew") }
    }
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
        name = "DevAuth"
        content { includeGroup("me.djtheredstoner") }
    }
}

loom {
    runs {
        named("client") {
            runDir = if (target == "26.1.2") "run" else "run-$target"
            property("devauth.enabled", project.findProperty("devauth")?.toString() ?: "true")
            // -Pcapture=false turns off the dev capture logger.
            project.findProperty("capture")?.let { property("eventhelper.capture", it.toString()) }
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    implementation("net.fabricmc.fabric-api:fabric-api:${targetProperty("fabric_version")}")

    implementation("dev.isxander:yet-another-config-lib:$yaclVersion")

    implementation("com.noxcrew.sheeplib:api:$sheeplibVersion")
    include("com.noxcrew.sheeplib:api:$sheeplibVersion")

    implementation("com.noxcrew.noxesium:fabric:$noxesiumVersion") { isTransitive = false }
    compileOnly("com.noxcrew.noxesium:api:$noxesiumVersion")

    compileOnly("com.terraformersmc:modmenu:${targetProperty("modmenu_version")}")
    localRuntime("com.terraformersmc:modmenu:${targetProperty("modmenu_version")}")

    localRuntime("me.djtheredstoner:DevAuth-fabric:${project.property("devauth_version")}")
}

val yaclMinVersion = yaclVersion.substringBefore('+')

tasks.processResources {
    val expansions = mapOf(
        "version" to project.version.toString(),
        "minecraft_range" to targetProperty("minecraft_range"),
        "loader_version" to project.property("loader_version") as String,
        "kotlin_loader_version" to project.property("kotlin_loader_version") as String,
        "yacl_version" to yaclMinVersion,
        "noxesium_version" to noxesiumVersion,
    )
    inputs.properties(expansions)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(expansions)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }
}
