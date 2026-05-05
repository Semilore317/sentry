plugins {
    kotlin("jvm") version "2.2.0"
    id("org.graalvm.buildtools.native") version "0.10.2"
}

group = "com.abraham_bankole"

description = "sentry"

repositories {
    mavenCentral()
}

dependencies {
    // JetBrains Koog
    implementation("ai.koog:koog-agents-jvm:0.8.0")
    implementation("ai.koog:koog-strategy-graph:0.8.0")
    implementation("ai.koog:koog-mcp:0.8.0") // For the "Agnostic" layer

    // Infra
    implementation("org.apache.kafka:kafka-clients:3.8.0")
    implementation("org.neo4j.driver:neo4j-java-driver:5.22.0") // Memgraph Bolt

    // Local AI Bridge
    implementation("ai.koog:koog-provider-ollama:0.8.0")
}

spotless{
    kotlin{
        ktfmt("0.5.3").googleStyle()
        trimTrailingWhitespace()
        endWithNewLine()
    }

    kotlinGradle{
        target("*.gradle.kts")
        ktfmt("0.5.3").googleStyle()
    }

    format("misc"){
        target("**/*.md", "**/.gitignore", "**/*.yml")
        leadingTabsToSpaces()
        trimTrailingWhitespace()
        endWithNewLine()
    }
}
