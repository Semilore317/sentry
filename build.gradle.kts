group = "com.abraham_bankole"

description = "sentry"

plugins {
  kotlin("jvm") version "2.2.0"
  id("org.graalvm.buildtools.native") version "0.10.2"
  id("com.diffplug.spotless") version "8.4.0"
  application
}

application {
  mainClass.set("com.abraham_bankole.MainKt")
}

kotlin {
  jvmToolchain(21)
}

repositories { mavenCentral() }

dependencies {
  // runtime dependencies
  implementation("ai.koog:koog-agents-jvm:0.7.1")
  implementation("ai.koog:agents-ext-jvm:0.7.1")
  implementation("ai.koog:agents-mcp-jvm:0.7.1")

  // infra
  implementation("org.apache.kafka:kafka-clients:3.8.0")
  implementation("org.neo4j.driver:neo4j-java-driver:5.22.0")

  // local ai bridge
  implementation("ai.koog:prompt-executor-ollama-client-jvm:0.7.1")
}

gradle.taskGraph.whenReady {
  allTasks.filterIsInstance<JavaExec>().forEach { task ->
    if (task.executable.contains("<<target java executable path>>")) {
      val toolchains = project.extensions.getByType<JavaToolchainService>()
      task.executable = toolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
      }.get().executablePath.asFile.absolutePath
    }
  }
}

spotless {
  kotlin {
    ktfmt("0.53").googleStyle()
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlinGradle {
    target("*.gradle.kts")
    ktfmt("0.53").googleStyle()
  }
  format("misc") {
    target("**/*.md", "**/.gitignore", "**/*.yml")
    leadingTabsToSpaces()
    trimTrailingWhitespace()
    endWithNewline()
  }
}
