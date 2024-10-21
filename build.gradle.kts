import org.gradle.api.JavaVersion
import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.bukkit.bukkitPluginYaml
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
  java
  `java-library`

  `maven-publish`

  id("io.papermc.paperweight.userdev") version "1.7.3"
  id("xyz.jpenilla.resource-factory-paper-convention") version "1.2.0"
  id("xyz.jpenilla.run-paper") version "2.3.1"

  id("io.github.goooler.shadow") version "8.1.8"
}

val groupStringSeparator = "."
val kebabCaseStringSeparator = "-"
val snakeCaseStringSeparator = "_"

fun capitalizeFirstLetter(string: String): String {
  return string.first().uppercase() + string.slice(IntRange(1, string.length - 1))
}

fun snakeCase(kebabCaseString: String): String {
  return kebabCaseString.lowercase().replace(kebabCaseStringSeparator, snakeCaseStringSeparator).replace(" ", snakeCaseStringSeparator)
}

fun pascalCase(string: String): String {
  var pascalCaseString = ""

  val splitString = string.split(Regex("$kebabCaseStringSeparator| "))

  for (part in splitString) {
    pascalCaseString += capitalizeFirstLetter(part)
  }

  return pascalCaseString
}

description = "A Minecraft Paper plugin that adds a firework-focused PvP game-mode."

val mainProjectAuthor = "Esoteric Organisation"
val simplifiedMainProjectAuthor = "Esoteric"

val projectAuthors = listOfNotNull(mainProjectAuthor, "rolyPolyVole", "Esoteric Enderman")

val topLevelDomain = "foundation"
val projectNameString = rootProject.name
val bootstrapperNameString = rootProject.name + "-bootstrapper"

group = topLevelDomain + groupStringSeparator + simplifiedMainProjectAuthor.lowercase().replace(" ", "")
version = "0.1.3"

val buildDirectoryString = layout.buildDirectory.toString()

val projectGroupString = group.toString()
val projectVersionString = version.toString()

val javaVersion = 21
val javaVersionEnumMember = JavaVersion.valueOf("VERSION_$javaVersion")
val paperApiVersion = "1.21"

java {
  sourceCompatibility = javaVersionEnumMember
  targetCompatibility = javaVersionEnumMember

  toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}

repositories {
    mavenCentral()
}

dependencies {
  paperweight.paperDevBundle("$paperApiVersion-R0.1-SNAPSHOT")

  implementation("dev.jorel" , "commandapi-bukkit-shade-mojang-mapped" , "9.5.1")
  implementation("org.apache.commons", "commons-compress", "1.24.0")
  implementation("org.tukaani", "xz", "1.9")
}

tasks {
  build {
    dependsOn(shadowJar)
  }

  shadowJar {
    archiveFileName = "$projectNameString-$projectVersionString.jar"
  }

  compileJava {
    options.release = javaVersion
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }
}

val projectName = providers.gradleProperty("projectName").get()

paperPluginYaml {
  name = pascalCase(projectName).replace(Regex(" Plugin$"), "")
  authors = projectAuthors

  main = projectGroupString + groupStringSeparator + "minecraft.plugins.games.fireworkwars" + groupStringSeparator + pascalCase(projectNameString)
  apiVersion = paperApiVersion
  description = project.description

  bootstrapper = projectGroupString + groupStringSeparator + "minecraft.plugins.games.fireworkwars" + groupStringSeparator + pascalCase(bootstrapperNameString)
}

bukkitPluginYaml {
  load = BukkitPluginYaml.PluginLoadOrder.STARTUP
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = projectGroupString
            artifactId = projectNameString
            version = projectVersionString
        }
    }
}

tasks.named("publishMavenJavaPublicationToMavenLocal") {
  dependsOn(tasks.named("build"))
}
